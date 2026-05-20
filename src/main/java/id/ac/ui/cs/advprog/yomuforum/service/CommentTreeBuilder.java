package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentComposite;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentLeaf;
import org.springframework.stereotype.Component;

import java.util.*;

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
public class CommentTreeBuilder {

    public List<CommentComponent> buildTree(List<Comment> flatComments) {
        if (flatComments == null || flatComments.isEmpty()) {
            return Collections.emptyList();
        }

        // Phase 1: Create a composite node for every comment
        Map<UUID, CommentComposite> nodeMap = new LinkedHashMap<>();
        for (Comment comment : flatComments) {
            nodeMap.put(comment.getId(), new CommentComposite(comment));
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
            return new CommentLeaf(extractComment(composite));
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
