package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody CommentRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        String resolvedUserId = resolveUserId(userIdHeader, request.getUserId());
        comment.setUserId(parseRequiredUuid(resolvedUserId, "userId"));
        comment.setReadingId(parseRequiredUuid(request.getReadingId(), "readingId"));
        comment.setParentCommentId(parseUuid(request.getParentCommentId()));

        Comment createdComment = commentService.createComment(comment);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestBody CommentRequest requestBody) {
        Comment comment = new Comment();
        comment.setContent(requestBody.getContent());
        String resolvedUserId = resolveUserId(userIdHeader, requestBody.getUserId());

        Comment updatedComment = commentService.updateComment(
                id, comment, parseRequiredUuid(resolvedUserId, "userId"), isAdmin(userRole));
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(value = "userId", required = false) String userIdParam) {
        String resolvedUserId = resolveUserId(userIdHeader, userIdParam);
        commentService.deleteComment(
                id, parseRequiredUuid(resolvedUserId, "userId"), isAdmin(userRole));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") UUID id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments() {
        List<Comment> comments = commentService.getAllComments();
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<Comment>> getCommentsByReadingId(@PathVariable("readingId") UUID readingId) {
        List<Comment> comments = commentService.getCommentsByReadingId(readingId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Comment>> getRepliesByParentId(@PathVariable("parentId") UUID parentId) {
        List<Comment> replies = commentService.getRepliesByParentId(parentId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUserId(@PathVariable("userId") UUID userId) {
        List<Comment> comments = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Composite Pattern: Returns nested comment tree for a reading.
     */
    @GetMapping("/reading/{readingId}/tree")
    public ResponseEntity<List<CommentComponent>> getCommentTree(@PathVariable("readingId") UUID readingId) {
        List<CommentComponent> tree = commentService.getCommentTreeByReadingId(readingId);
        return ResponseEntity.ok(tree);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Invalid UUID format: " + value);
        }
    }

    private UUID parseRequiredUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(fieldName + " is required");
        }
        return parseUuid(value);
    }

    private boolean isAdmin(String userRole) {
        return userRole != null && userRole.equalsIgnoreCase("ADMIN");
    }

    /**
     * Resolves userId: prefers header, falls back to body/param.
     */
    private String resolveUserId(String headerValue, String bodyValue) {
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        return bodyValue;
    }
}