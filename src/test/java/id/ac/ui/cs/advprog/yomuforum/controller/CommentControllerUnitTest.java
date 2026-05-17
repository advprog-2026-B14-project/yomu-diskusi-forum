package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerUnitTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private UUID commentId;
    private UUID userId;
    private UUID readingId;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        readingId = UUID.randomUUID();

        comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setReadingId(readingId);
        comment.setContent("Test comment");
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
    }

    // ================ createComment ================

    @Test
    void testCreateComment() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());
        request.setParentCommentId(UUID.randomUUID().toString());

        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Comment> response = commentController.createComment(userId.toString(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(comment, response.getBody());
        verify(commentService).createComment(any(Comment.class));
    }

    @Test
    void testCreateCommentWithoutParentId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());

        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Comment> response = commentController.createComment(userId.toString(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreateCommentWithBlankParentId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());
        request.setParentCommentId("  ");

        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Comment> response = commentController.createComment(userId.toString(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreateCommentWithInvalidParentId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());
        request.setParentCommentId("not-a-uuid");

        assertThrows(InvalidInputException.class,
                () -> commentController.createComment(userId.toString(), request));
    }

    @Test
    void testCreateCommentWithMissingUserId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());

        assertThrows(InvalidInputException.class,
                () -> commentController.createComment(null, request));
    }

    @Test
    void testCreateCommentWithBlankUserId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());

        assertThrows(InvalidInputException.class,
                () -> commentController.createComment("   ", request));
    }

    @Test
    void testCreateCommentWithInvalidUserIdFormat() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(readingId.toString());

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentController.createComment("not-a-uuid", request));

        assertEquals("Invalid UUID format: not-a-uuid", exception.getMessage());
    }

    @Test
    void testCreateCommentWithBlankReadingId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setReadingId(" ");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentController.createComment(userId.toString(), request));

        assertEquals("readingId is required", exception.getMessage());
    }

    @Test
    void testCreateCommentWithMissingReadingId() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentController.createComment(userId.toString(), request));

        assertEquals("readingId is required", exception.getMessage());
    }

    // ================ updateComment ================

    @Test
    void testUpdateComment() {
        CommentRequest request = new CommentRequest();
        request.setContent("Updated");

        Comment updated = new Comment();
        updated.setId(commentId);
        updated.setContent("Updated");
        when(commentService.updateComment(eq(commentId), any(Comment.class), eq(userId), eq(false)))
                .thenReturn(updated);

        ResponseEntity<Comment> response = commentController.updateComment(
                commentId, userId.toString(), null, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getContent());
    }

    @Test
    void testUpdateCommentAsAdmin() {
        CommentRequest request = new CommentRequest();
        request.setContent("Admin edit");

        Comment updated = new Comment();
        updated.setId(commentId);
        updated.setContent("Admin edit");
        when(commentService.updateComment(eq(commentId), any(Comment.class), eq(userId), eq(true)))
                .thenReturn(updated);

        ResponseEntity<Comment> response = commentController.updateComment(
                commentId, userId.toString(), "ADMIN", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService).updateComment(eq(commentId), any(Comment.class), eq(userId), eq(true));
    }

    @Test
    void testUpdateCommentWithNullUserId() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");

        assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, null, null, request));
    }

    @Test
    void testUpdateCommentWithBlankUserId() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, "   ", null, request));
        assertEquals("X-User-Id is required", ex.getMessage());
    }

    @Test
    void testUpdateCommentWithInvalidUuidUserId() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, "not-a-valid-uuid", null, request));
        assertEquals("Invalid UUID format: not-a-valid-uuid", ex.getMessage());
    }

    // ================ deleteComment ================

    @Test
    void testDeleteComment() {
        doNothing().when(commentService).deleteComment(commentId, userId, false);

        ResponseEntity<Void> response = commentController.deleteComment(commentId, userId.toString(), null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commentService).deleteComment(commentId, userId, false);
    }

    @Test
    void testDeleteCommentAsAdmin() {
        doNothing().when(commentService).deleteComment(commentId, userId, true);

        ResponseEntity<Void> response = commentController.deleteComment(commentId, userId.toString(), "ADMIN");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commentService).deleteComment(commentId, userId, true);
    }

    @Test
    void testDeleteCommentWithNullUserId() {
        assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, null, null));
    }

    @Test
    void testDeleteCommentWithBlankUserId() {
        assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, "", null));
    }

    @Test
    void testDeleteCommentWithInvalidUuidUserId() {
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, "bad-uuid", null));
        assertEquals("Invalid UUID format: bad-uuid", ex.getMessage());
    }

    // ================ getCommentById ================

    @Test
    void testGetCommentById() {
        when(commentService.getCommentById(commentId)).thenReturn(comment);

        ResponseEntity<Comment> response = commentController.getCommentById(commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(comment, response.getBody());
    }

    // ================ getAllComments ================

    @Test
    void testGetAllComments() {
        Comment c2 = new Comment();
        c2.setId(UUID.randomUUID());
        c2.setContent("Another");

        when(commentService.getAllComments()).thenReturn(Arrays.asList(comment, c2));

        ResponseEntity<List<Comment>> response = commentController.getAllComments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // ================ getCommentsByReadingId ================

    @Test
    void testGetCommentsByReadingId() {
        when(commentService.getCommentsByReadingId(readingId)).thenReturn(List.of(comment));

        ResponseEntity<List<Comment>> response = commentController.getCommentsByReadingId(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ================ getRepliesByParentId ================

    @Test
    void testGetRepliesByParentId() {
        UUID parentId = UUID.randomUUID();
        Comment reply = new Comment();
        reply.setId(UUID.randomUUID());
        reply.setParentCommentId(parentId);
        reply.setContent("Reply");

        when(commentService.getRepliesByParentId(parentId)).thenReturn(List.of(reply));

        ResponseEntity<List<Comment>> response = commentController.getRepliesByParentId(parentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ================ getCommentsByUserId ================

    @Test
    void testGetCommentsByUserId() {
        when(commentService.getCommentsByUserId(userId)).thenReturn(List.of(comment));

        ResponseEntity<List<Comment>> response = commentController.getCommentsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ================ getCommentTree (Composite Pattern) ================

    @Test
    void testGetCommentTree() {
        CommentComponent mockComponent = mock(CommentComponent.class);
        when(commentService.getCommentTreeByReadingId(readingId)).thenReturn(List.of(mockComponent));

        ResponseEntity<List<CommentComponent>> response = commentController.getCommentTree(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(commentService).getCommentTreeByReadingId(readingId);
    }

    @Test
    void testGetCommentTreeEmpty() {
        when(commentService.getCommentTreeByReadingId(readingId)).thenReturn(List.of());

        ResponseEntity<List<CommentComponent>> response = commentController.getCommentTree(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
