package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    Comment createComment(Comment comment);
    Comment updateComment(UUID id, Comment comment, UUID actorUserId, boolean isAdmin);
    void deleteComment(UUID id, UUID actorUserId, boolean isAdmin);
    Comment getCommentById(UUID id);
    List<Comment> getAllComments();
    List<Comment> getCommentsByReadingId(UUID readingId);
    List<Comment> getRepliesByParentId(UUID parentCommentId);
    List<Comment> getCommentsByUserId(UUID userId);
}
