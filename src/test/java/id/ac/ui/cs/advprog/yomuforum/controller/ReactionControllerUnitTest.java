package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.dto.ReactionRequest;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionControllerUnitTest {

    @Mock
    private ReactionService reactionService;

    @InjectMocks
    private ReactionController reactionController;

    private UUID reactionId;
    private UUID commentId;
    private UUID userId;
    private Reaction reaction;

    @BeforeEach
    void setUp() {
        reactionId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reaction = new Reaction();
        reaction.setId(reactionId);
        reaction.setCommentId(commentId);
        reaction.setUserId(userId);
        reaction.setReactionType(ReactionType.UPVOTE);
        reaction.setCreatedAt(new Date());
    }

    @Test
    void testAddReaction() {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        when(reactionService.addReaction(any(Reaction.class))).thenReturn(reaction);

        ResponseEntity<Reaction> response = reactionController.addReaction(userId.toString(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(reaction, response.getBody());
        verify(reactionService).addReaction(any(Reaction.class));
    }

    @Test
    void testAddReactionWithNullUserId() {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionController.addReaction(null, request));

        assertEquals("X-User-Id is required", exception.getMessage());
    }

    @Test
    void testAddReactionWithBlankUserId() {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionController.addReaction(" ", request));

        assertEquals("X-User-Id is required", exception.getMessage());
    }

    @Test
    void testAddReactionWithInvalidUserId() {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionController.addReaction("not-a-uuid", request));

        assertEquals("Invalid UUID format: not-a-uuid", exception.getMessage());
    }

    @Test
    void testRemoveReaction() {
        doNothing().when(reactionService).removeReaction(reactionId, userId, false);

        ResponseEntity<Void> response = reactionController.removeReaction(reactionId, userId.toString(), null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reactionService).removeReaction(reactionId, userId, false);
    }

    @Test
    void testRemoveReactionAsAdmin() {
        doNothing().when(reactionService).removeReaction(eq(reactionId), eq(userId), eq(true));

        ResponseEntity<Void> response = reactionController.removeReaction(reactionId, userId.toString(), "ADMIN");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reactionService).removeReaction(eq(reactionId), eq(userId), eq(true));
    }

    @Test
    void testRemoveReactionWithNullUserId() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionController.removeReaction(reactionId, null, null));

        assertEquals("X-User-Id is required", exception.getMessage());
        verify(reactionService, never()).removeReaction(any(), any(), anyBoolean());
    }

    @Test
    void testRemoveReactionWithBlankUserId() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionController.removeReaction(reactionId, " ", null));

        assertEquals("X-User-Id is required", exception.getMessage());
        verify(reactionService, never()).removeReaction(any(), any(), anyBoolean());
    }
}
