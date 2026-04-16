package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @Mock
    private ReactionRepository reactionRepository;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    private Reaction reaction;
    private UUID reactionId;
    private UUID commentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        reactionId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reaction = new Reaction();
        reaction.setId(reactionId);
        reaction.setCommentId(commentId);
        reaction.setUserId(userId);
        reaction.setReactionType("LIKE");
    }

    @Test
    void testAddReactionNoExisting() {
        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reaction result = reactionService.addReaction(reaction);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        verify(reactionRepository, times(1)).findByCommentIdAndUserId(commentId, userId);
        verify(reactionRepository, never()).delete(any(Reaction.class));
        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    void testAddReactionWithExistingReaction() {
        Reaction existingReaction = new Reaction();
        existingReaction.setId(UUID.randomUUID());
        existingReaction.setCommentId(commentId);
        existingReaction.setUserId(userId);
        existingReaction.setReactionType("DISLIKE");

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.of(existingReaction));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reaction result = reactionService.addReaction(reaction);

        assertNotNull(result);
        verify(reactionRepository, times(1)).delete(existingReaction);
        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    void testRemoveReaction() {
        doNothing().when(reactionRepository).deleteById(reactionId);

        reactionService.removeReaction(reactionId);

        verify(reactionRepository, times(1)).deleteById(reactionId);
    }

    @Test
    void testGetReactionsByCommentId() {
        Reaction reaction2 = new Reaction();
        reaction2.setId(UUID.randomUUID());
        reaction2.setCommentId(commentId);
        reaction2.setUserId(UUID.randomUUID());
        reaction2.setReactionType("DISLIKE");

        when(reactionRepository.findByCommentId(commentId))
                .thenReturn(Arrays.asList(reaction, reaction2));

        List<Reaction> results = reactionService.getReactionsByCommentId(commentId);

        assertEquals(2, results.size());
        verify(reactionRepository, times(1)).findByCommentId(commentId);
    }

    @Test
    void testGetReactionsByCommentIdEmpty() {
        when(reactionRepository.findByCommentId(commentId)).thenReturn(List.of());

        List<Reaction> results = reactionService.getReactionsByCommentId(commentId);

        assertTrue(results.isEmpty());
    }

    @Test
    void testCountReactionsByType() {
        when(reactionRepository.countByCommentIdAndReactionType(commentId, "LIKE"))
                .thenReturn(5L);

        long count = reactionService.countReactionsByType(commentId, "LIKE");

        assertEquals(5L, count);
        verify(reactionRepository, times(1))
                .countByCommentIdAndReactionType(commentId, "LIKE");
    }

    @Test
    void testCountReactionsByTypeZero() {
        when(reactionRepository.countByCommentIdAndReactionType(commentId, "DISLIKE"))
                .thenReturn(0L);

        long count = reactionService.countReactionsByType(commentId, "DISLIKE");

        assertEquals(0L, count);
    }

    @Test
    void testGetUserReactionFound() {
        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.of(reaction));

        Reaction result = reactionService.getUserReaction(commentId, userId);

        assertNotNull(result);
        assertEquals(reaction.getId(), result.getId());
        assertEquals("LIKE", result.getReactionType());
        verify(reactionRepository, times(1)).findByCommentIdAndUserId(commentId, userId);
    }

    @Test
    void testGetUserReactionNotFound() {
        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.empty());

        Reaction result = reactionService.getUserReaction(commentId, userId);

        assertNull(result);
        verify(reactionRepository, times(1)).findByCommentIdAndUserId(commentId, userId);
    }
}
