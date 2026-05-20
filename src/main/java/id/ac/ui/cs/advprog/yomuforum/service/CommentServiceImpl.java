package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.exception.CommentNotFoundException;
import id.ac.ui.cs.advprog.yomuforum.exception.ForbiddenException;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.model.observer.CommentEvent;
import id.ac.ui.cs.advprog.yomuforum.model.observer.CommentEventPublisher;
import id.ac.ui.cs.advprog.yomuforum.model.observer.EventType;
import id.ac.ui.cs.advprog.yomuforum.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final String COMMENT_NOT_FOUND_MESSAGE = "Comment not found";

    private final CommentRepository commentRepository;
    private final CommentTreeBuilder commentTreeBuilder;
    private final CommentEventPublisher commentEventPublisher;

    @Override
    public Comment createComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new InvalidInputException("Content cannot be empty");
        }
        if (comment.getUserId() == null || comment.getReadingId() == null) {
            throw new InvalidInputException("userId and readingId are required");
        }

        comment.setId(UUID.randomUUID());
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        Comment saved = commentRepository.save(comment);

        // Observer Pattern: notify subscribers about new comment
        commentEventPublisher.notifySubscribers(
                new CommentEvent(EventType.COMMENT_CREATED, saved.getId(), saved.getUserId(),
                        "Comment created on reading " + saved.getReadingId()));

        return saved;
    }

    @Override
    public Comment updateComment(UUID id, Comment comment, UUID actorUserId, boolean isAdmin) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE));

        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new InvalidInputException("Content cannot be empty");
        }

        if (actorUserId == null) {
            throw new InvalidInputException("userId is required");
        }

        if (!isAdmin && !existingComment.getUserId().equals(actorUserId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        existingComment.setContent(comment.getContent());
        existingComment.setUpdatedAt(new Date());
        Comment saved = commentRepository.save(existingComment);

        // Observer Pattern: notify subscribers about comment update
        commentEventPublisher.notifySubscribers(
                new CommentEvent(EventType.COMMENT_UPDATED, saved.getId(), actorUserId,
                        "Comment updated"));

        return saved;
    }

    @Override
    public void deleteComment(UUID id, UUID actorUserId, boolean isAdmin) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE));

        if (actorUserId == null) {
            throw new InvalidInputException("userId is required");
        }

        if (!isAdmin && !existingComment.getUserId().equals(actorUserId)) {
            throw new ForbiddenException("You can only delete your own comments unless you are admin");
        }

        commentRepository.deleteById(id);

        // Observer Pattern: notify subscribers about comment deletion
        commentEventPublisher.notifySubscribers(
                new CommentEvent(EventType.COMMENT_DELETED, id, actorUserId,
                        "Comment deleted"));
    }

    @Override
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE));
    }

    @Override
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @Override
    public List<Comment> getCommentsByReadingId(UUID readingId) {
        return commentRepository.findByReadingId(readingId);
    }

    @Override
    public List<Comment> getRepliesByParentId(UUID parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    @Override
    public List<Comment> getCommentsByUserId(UUID userId) {
        return commentRepository.findByUserId(userId);
    }

    /**
     * Composite Pattern: Builds a nested comment tree for a given reading.
     * Uses CommentTreeBuilder to convert flat list into tree structure.
     */
    @Override
    public List<CommentComponent> getCommentTreeByReadingId(UUID readingId) {
        List<Comment> flatComments = commentRepository.findByReadingId(readingId);
        return commentTreeBuilder.buildTree(flatComments);
    }
}
