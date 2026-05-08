package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.exception.CommentNotFoundException;
import id.ac.ui.cs.advprog.yomuforum.exception.ForbiddenException;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

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
        return commentRepository.save(comment);
    }

    @Override
    public Comment updateComment(UUID id, Comment comment, UUID actorUserId, boolean isAdmin) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

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
        return commentRepository.save(existingComment);
    }

    @Override
    public void deleteComment(UUID id, UUID actorUserId, boolean isAdmin) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (actorUserId == null) {
            throw new InvalidInputException("userId is required");
        }

        if (!isAdmin && !existingComment.getUserId().equals(actorUserId)) {
            throw new ForbiddenException("You can only delete your own comments unless you are admin");
        }

        commentRepository.deleteById(id);
    }

    @Override
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
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
}
