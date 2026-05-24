package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComposite;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentLeaf;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

@Component
public class CommentTreeBuilder {

    public List<CommentComponent> buildTree(List<Comment> flatComments) {
        if (flatComments == null || flatComments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, CommentComposite> nodeMap = new LinkedHashMap<>();
        for (Comment comment : flatComments) {
            nodeMap.put(comment.getId(), new CommentComposite(comment));
        }

        List<CommentComposite> roots = new ArrayList<>();
        Set<UUID> parentsWithChildren = new HashSet<>();

        for (Comment comment : flatComments) {
            UUID parentId = comment.getParentCommentId();
            CommentComposite node = nodeMap.get(comment.getId());

            if (parentId == null || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                CommentComposite parent = nodeMap.get(parentId);
                parent.addChild(node);
                parentsWithChildren.add(parentId);
            }
        }

        List<CommentComponent> result = new ArrayList<>();
        for (CommentComposite root : roots) {
            result.add(convertToLeafIfNeeded(root, parentsWithChildren));
        }
        return result;
    }

    private CommentComponent convertToLeafIfNeeded(CommentComposite composite, Set<UUID> parentsWithChildren) {
        if (!parentsWithChildren.contains(composite.getId())) {
            return new CommentLeaf(extractComment(composite));
        }

        List<CommentComponent> processedChildren = new ArrayList<>();
        for (CommentComponent child : composite.getChildren()) {
            if (child instanceof CommentComposite childComposite) {
                processedChildren.add(convertToLeafIfNeeded(childComposite, parentsWithChildren));
            } else {
                processedChildren.add(child);
            }
        }

        CommentComposite newComposite = new CommentComposite(extractComment(composite));
        for (CommentComponent processedChild : processedChildren) {
            newComposite.addChild(processedChild);
        }
        return newComposite;
    }

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
