package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.service.AsyncCommentService;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final AsyncCommentService asyncCommentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody CommentRequest request) {

        String resolvedUserId = resolveRequiredUserId(userIdHeader);
        Comment comment = new Comment();
        comment.setContent(request.getContent());
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

        String resolvedUserId = resolveRequiredUserId(userIdHeader);
        Comment comment = new Comment();
        comment.setContent(requestBody.getContent());

        Comment updatedComment = commentService.updateComment(
                id,
                comment,
                parseRequiredUuid(resolvedUserId, "userId"),
                isAdmin(userRole)
        );
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        String resolvedUserId = resolveRequiredUserId(userIdHeader);
        commentService.deleteComment(
                id,
                parseRequiredUuid(resolvedUserId, "userId"),
                isAdmin(userRole)
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<Comment>> getCommentsByReadingId(@PathVariable("readingId") UUID readingId) {
        return ResponseEntity.ok(commentService.getCommentsByReadingId(readingId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Comment>> getRepliesByParentId(@PathVariable("parentId") UUID parentId) {
        return ResponseEntity.ok(commentService.getRepliesByParentId(parentId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUserId(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    @GetMapping("/reading/{readingId}/tree")
    public ResponseEntity<List<CommentComponent>> getCommentTree(@PathVariable("readingId") UUID readingId) {
        return ResponseEntity.ok(commentService.getCommentTreeByReadingId(readingId));
    }

    @GetMapping("/reading/{readingId}/async-tree")
    public CompletableFuture<Map<String, Object>> getCommentTreeAsync(
            @PathVariable("readingId") UUID readingId) {

        CompletableFuture<List<CommentComponent>> treeFuture =
                asyncCommentService.getCommentTreeAsync(readingId);
        CompletableFuture<List<Comment>> flatFuture =
                asyncCommentService.getCommentsByReadingIdAsync(readingId);

        return treeFuture.thenCombine(flatFuture, (tree, flat) ->
                Map.of(
                        "tree", tree,
                        "flatComments", flat,
                        "totalComments", flat.size()
                )
        );
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

    private String resolveRequiredUserId(String headerValue) {
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        throw new InvalidInputException("userId is required");
    }
}