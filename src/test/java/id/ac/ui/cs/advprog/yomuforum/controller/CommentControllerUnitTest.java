package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
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

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
    }

    @Test
    void testUpdateCommentWithNullPrincipal() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, null, httpRequest, request));
    }

    @Test
    void testUpdateCommentWithPrincipalNameNull() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(null);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, principal, httpRequest, request));
        assertEquals("Authenticated user is required", ex.getMessage());
    }

    @Test
    void testUpdateCommentWithPrincipalNameBlank() {
        CommentRequest request = new CommentRequest();
        request.setContent("content");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("   ");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                commentController.updateComment(commentId, principal, httpRequest, request));
        assertEquals("Authenticated user is required", ex.getMessage());
    }

    @Test
    void testDeleteCommentWithNullPrincipal() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, null, httpRequest));
    }

    @Test
    void testDeleteCommentWithPrincipalNameNull() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(null);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, principal, httpRequest));
    }

    @Test
    void testDeleteCommentWithPrincipalNameBlank() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThrows(InvalidInputException.class, () ->
                commentController.deleteComment(commentId, principal, httpRequest));
    }

    @Test
    void testIsAdminWithNullRequest() {
        UUID userId = UUID.randomUUID();
        CommentRequest request = new CommentRequest();
        request.setContent("content");

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userId.toString());

        Comment updated = new Comment();
        updated.setContent("content");
        when(commentService.updateComment(eq(commentId), any(Comment.class), eq(userId), eq(false)))
                .thenReturn(updated);

        // Passing null HttpServletRequest triggers the isAdmin null-check
        ResponseEntity<Comment> response = commentController.updateComment(commentId, principal, null, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService).updateComment(eq(commentId), any(Comment.class), eq(userId), eq(false));
    }

    @Test
    void testDeleteWithNullHttpServletRequest() {
        UUID userId = UUID.randomUUID();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userId.toString());
        doNothing().when(commentService).deleteComment(commentId, userId, false);

        ResponseEntity<Void> response = commentController.deleteComment(commentId, principal, null);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commentService).deleteComment(commentId, userId, false);
    }
}
