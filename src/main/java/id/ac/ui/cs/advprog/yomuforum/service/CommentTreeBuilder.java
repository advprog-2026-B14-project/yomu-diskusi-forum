package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComposite;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentLeaf;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Utility service yang membangun tree (pohon) dari flat list komentar.
 * Menggunakan Composite Pattern untuk membentuk struktur parent-child.
 *
 * Algoritma:
 * 1. Buat CommentComposite untuk setiap komentar (assume semua bisa punya anak)
 * 2. Link setiap anak ke parent-nya berdasarkan parentCommentId
 * 3. Komentar yang tidak punya anak diubah menjadi CommentLeaf
 * 4. Return root-level comments (yang parentCommentId == null)
 */
@Component
@RequiredArgsConstructor
public class CommentTreeBuilder {

    private final ReactionRepository reactionRepository;

    public List<CommentComponent> buildTree(List<Comment> flatComments) {
        if (flatComments == null || flatComments.isEmpty()) {
            return Collections.emptyList();
        }

        // Phase 1: Fetch ALL reactions in ONE SINGLE QUERY (Optimized / Fix for N+1 Query Problem)
        List<UUID> commentIds = flatComments.stream().map(Comment::getId).toList();
        List<Object[]> reactionCounts = reactionRepository.countReactionsForComments(commentIds);
        
        // Memetakan hasil query ke dalam struktur Map untuk pencarian cepat (O(1))
        Map<UUID, Map<ReactionType, Long>> reactionMap = new LinkedHashMap<>();
        for (Object[] row : reactionCounts) {
            UUID cId = (UUID) row[0];
            ReactionType type = (ReactionType) row[1];
            long count = (Long) row[2];
            reactionMap.computeIfAbsent(cId, k -> new LinkedHashMap<>()).put(type, count);
        }

        // Phase 2: Create a composite node for every comment
        Map<UUID, CommentComposite> nodeMap = new LinkedHashMap<>();
        for (Comment comment : flatComments) {
            CommentComposite composite = new CommentComposite(comment);
            
            // OPTIMIZED (V2) - Menarik data dari Map memory, BUKAN nembak ke database!
            Map<ReactionType, Long> counts = reactionMap.getOrDefault(comment.getId(), Collections.emptyMap());
            composite.setUpvotes(counts.getOrDefault(ReactionType.UPVOTE, 0L));
            composite.setDownvotes(counts.getOrDefault(ReactionType.DOWNVOTE, 0L));
            
            /* 
             * ============================================================================
             * NAIVE IMPLEMENTATION (V1) - N+1 QUERY PROBLEM (Dipakai buat before-refactor)
             * ============================================================================
             * long upvotes = reactionRepository.countByCommentIdAndReactionType(comment.getId(), ReactionType.UPVOTE);
             * long downvotes = reactionRepository.countByCommentIdAndReactionType(comment.getId(), ReactionType.DOWNVOTE);
             * composite.setUpvotes(upvotes);
             * composite.setDownvotes(downvotes);
             */
            
            nodeMap.put(comment.getId(), composite);
        }

        // Phase 2: Link children to parents
        List<CommentComposite> roots = new ArrayList<>();
        Set<UUID> parentsWithChildren = new HashSet<>();

        for (Comment comment : flatComments) {
            UUID parentId = comment.getParentCommentId();
            CommentComposite node = nodeMap.get(comment.getId());

            if (parentId == null || !nodeMap.containsKey(parentId)) {
                // Root-level comment
                roots.add(node);
            } else {
                // Attach to parent
                CommentComposite parent = nodeMap.get(parentId);
                parent.addChild(node);
                parentsWithChildren.add(parentId);
            }
        }

        // Phase 3: Convert childless composites to leaves (for correct isLeaf() behavior)
        List<CommentComponent> result = new ArrayList<>();
        for (CommentComposite root : roots) {
            result.add(convertToLeafIfNeeded(root, parentsWithChildren));
        }
        return result;
    }

    private CommentComponent convertToLeafIfNeeded(CommentComposite composite, Set<UUID> parentsWithChildren) {
        if (!parentsWithChildren.contains(composite.getId())) {
            // This node has no children → convert to leaf
            CommentLeaf leaf = new CommentLeaf(extractComment(composite));
            leaf.setUpvotes(composite.getUpvotes());
            leaf.setDownvotes(composite.getDownvotes());
            return leaf;
        }

        // Recursively process children
        List<CommentComponent> processedChildren = new ArrayList<>();
        for (CommentComponent child : composite.getChildren()) {
            if (child instanceof CommentComposite childComposite) {
                processedChildren.add(convertToLeafIfNeeded(childComposite, parentsWithChildren));
            } else {
                processedChildren.add(child);
            }
        }

        // Rebuild composite with processed children
        CommentComposite newComposite = new CommentComposite(extractComment(composite));
        newComposite.setUpvotes(composite.getUpvotes());
        newComposite.setDownvotes(composite.getDownvotes());
        for (CommentComponent processedChild : processedChildren) {
            newComposite.addChild(processedChild);
        }
        return newComposite;
    }

    /**
     * Helper to reconstruct a Comment entity from a CommentComponent.
     */
    private Comment extractComment(CommentComponent component) {
        Comment comment = new Comment();
        comment.setId(component.getId());
        comment.setUserId(component.getUserId());
        comment.setReadingId(component.getReadingId());
        comment.setParentCommentId(component.getParentCommentId());
        comment.setContent(component.getContent());
        comment.setCreatedAt(component.getCreatedAt());
        comment.setUpdatedAt(component.getUpdatedAt());
        return comment;
    }
}
