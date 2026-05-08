package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody CommentRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUserId(parseRequiredUuid(request.getUserId(), "userId"));
        comment.setReadingId(parseRequiredUuid(request.getReadingId(), "readingId"));
        comment.setParentCommentId(parseUuid(request.getParentCommentId()));

        Comment createdComment = commentService.createComment(comment);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("id") UUID id,
            Principal principal,
            HttpServletRequest request,
            @RequestBody CommentRequest requestBody) {
        Comment comment = new Comment();
        comment.setContent(requestBody.getContent());

        Comment updatedComment = commentService.updateComment(id, comment, extractUserId(principal), isAdmin(request));
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") UUID id,
            Principal principal,
            HttpServletRequest request) {
        commentService.deleteComment(id, extractUserId(principal), isAdmin(request));
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

    private UUID extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new InvalidInputException("Authenticated user is required");
        }

        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Authenticated user id is not a valid UUID");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        return request != null && request.isUserInRole("ADMIN");
    }
}