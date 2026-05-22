package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import id.ac.ui.cs.advprog.yomuforum.service.AsyncCommentService;

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
    private static final String USER_ID = "userId";

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody CommentRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        String resolvedUserId = resolveUserId(userIdHeader, request.getUserId());
        comment.setUserId(parseRequiredUuid(resolvedUserId, USER_ID));
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
            id, comment, parseRequiredUuid(resolvedUserId, USER_ID), isAdmin(userRole));
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(value = USER_ID, required = false) String userIdParam) {
        String resolvedUserId = resolveUserId(userIdHeader, userIdParam);
        commentService.deleteComment(
                id, parseRequiredUuid(resolvedUserId, USER_ID), isAdmin(userRole));
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

    /**
     * Async Parallel Endpoint: Mengambil comment tree dan reactions secara bersamaan.
     * Menggunakan CompletableFuture untuk menjalankan 2 query secara parallel
     * di thread pool terpisah, sehingga total waktu = max(query1, query2)
     * bukan query1 + query2.
     */
    @GetMapping("/reading/{readingId}/async-tree")
    public CompletableFuture<Map<String, Object>> getCommentTreeAsync(
            @PathVariable("readingId") UUID readingId) {
        // Jalankan kedua operasi di thread terpisah secara parallel
        CompletableFuture<List<CommentComponent>> treeFuture =
                asyncCommentService.getCommentTreeAsync(readingId);
        CompletableFuture<List<Comment>> flatFuture =
                asyncCommentService.getCommentsByReadingIdAsync(readingId);

        // Combine hasil dari kedua thread setelah keduanya selesai
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
