package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.event.CommentEvent;
import id.ac.ui.cs.advprog.yomuforum.event.CommentEventPublisher;
import id.ac.ui.cs.advprog.yomuforum.service.strategy.ReactionStrategy;
import id.ac.ui.cs.advprog.yomuforum.service.strategy.ReactionStrategyContext;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import id.ac.ui.cs.advprog.yomuforum.exception.ForbiddenException;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.exception.ReactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
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

    @Mock
    private ReactionStrategyContext reactionStrategyContext;

    @Mock
    private CommentEventPublisher commentEventPublisher;

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
        reaction.setReactionType(ReactionType.UPVOTE);
    }

    @Test
    void testAddReactionDelegatesToStrategy() {
        // The saved reaction returned by the strategy
        Reaction savedReaction = new Reaction();
        savedReaction.setId(UUID.randomUUID());
        savedReaction.setCommentId(commentId);
        savedReaction.setUserId(userId);
        savedReaction.setReactionType(ReactionType.UPVOTE);
        savedReaction.setCreatedAt(new Date());

        ReactionStrategy mockStrategy = mock(ReactionStrategy.class);
        when(reactionStrategyContext.getStrategy(ReactionType.UPVOTE)).thenReturn(mockStrategy);
        when(mockStrategy.apply(reaction, reactionRepository)).thenReturn(savedReaction);

        Reaction result = reactionService.addReaction(reaction);

        assertNotNull(result);
        assertEquals(savedReaction.getId(), result.getId());
        verify(reactionStrategyContext).getStrategy(ReactionType.UPVOTE);
        verify(mockStrategy).apply(reaction, reactionRepository);
        verify(commentEventPublisher).notifySubscribers(any(CommentEvent.class));
    }

    @Test
    void testAddReactionWithEmojiType() {
        reaction.setReactionType(ReactionType.EMOJI_HEART);

        Reaction savedReaction = new Reaction();
        savedReaction.setId(UUID.randomUUID());
        savedReaction.setCommentId(commentId);
        savedReaction.setUserId(userId);
        savedReaction.setReactionType(ReactionType.EMOJI_HEART);
        savedReaction.setCreatedAt(new Date());

        ReactionStrategy mockStrategy = mock(ReactionStrategy.class);
        when(reactionStrategyContext.getStrategy(ReactionType.EMOJI_HEART)).thenReturn(mockStrategy);
        when(mockStrategy.apply(reaction, reactionRepository)).thenReturn(savedReaction);

        Reaction result = reactionService.addReaction(reaction);

        assertNotNull(result);
        verify(reactionStrategyContext).getStrategy(ReactionType.EMOJI_HEART);
    }

    @Test
    void testRemoveReaction() {
        doNothing().when(reactionRepository).deleteById(reactionId);

        reaction.setUserId(userId);
        when(reactionRepository.findById(reactionId)).thenReturn(Optional.of(reaction));

        reactionService.removeReaction(reactionId, userId, false);

        verify(reactionRepository, times(1)).deleteById(reactionId);
        verify(commentEventPublisher).notifySubscribers(any(CommentEvent.class));
    }

    @Test
    void testRemoveReactionAsAdmin() {
        doNothing().when(reactionRepository).deleteById(reactionId);

        reaction.setUserId(UUID.randomUUID());
        when(reactionRepository.findById(reactionId)).thenReturn(Optional.of(reaction));

        reactionService.removeReaction(reactionId, userId, true);

        verify(reactionRepository, times(1)).deleteById(reactionId);
    }

    @Test
    void testRemoveReactionForbidden() {
        reaction.setUserId(UUID.randomUUID());
        when(reactionRepository.findById(reactionId)).thenReturn(Optional.of(reaction));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> reactionService.removeReaction(reactionId, userId, false));

        assertEquals("You can only delete your own reactions unless you are admin", exception.getMessage());
        verify(reactionRepository, never()).deleteById(reactionId);
    }

    @Test
    void testRemoveReactionNotFound() {
        when(reactionRepository.findById(reactionId)).thenReturn(Optional.empty());

        ReactionNotFoundException exception = assertThrows(ReactionNotFoundException.class,
                () -> reactionService.removeReaction(reactionId, userId, false));

        assertEquals("Reaction not found", exception.getMessage());
        verify(reactionRepository, never()).deleteById(reactionId);
    }

    @Test
    void testRemoveReactionMissingActorUserId() {
        reaction.setUserId(userId);
        when(reactionRepository.findById(reactionId)).thenReturn(Optional.of(reaction));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> reactionService.removeReaction(reactionId, null, false));

        assertEquals("userId is required", exception.getMessage());
        verify(reactionRepository, never()).deleteById(reactionId);
    }

    @Test
    void testGetReactionsByCommentId() {
        Reaction reaction2 = new Reaction();
        reaction2.setId(UUID.randomUUID());
        reaction2.setCommentId(commentId);
        reaction2.setUserId(UUID.randomUUID());
        reaction2.setReactionType(ReactionType.DOWNVOTE);

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
        when(reactionRepository.countByCommentIdAndReactionType(commentId, ReactionType.UPVOTE))
                .thenReturn(5L);

        long count = reactionService.countReactionsByType(commentId, "UPVOTE");

        assertEquals(5L, count);
        verify(reactionRepository, times(1))
            .countByCommentIdAndReactionType(commentId, ReactionType.UPVOTE);
    }

    @Test
    void testCountReactionsByTypeZero() {
        when(reactionRepository.countByCommentIdAndReactionType(commentId, ReactionType.DOWNVOTE))
                .thenReturn(0L);

        long count = reactionService.countReactionsByType(commentId, "DOWNVOTE");

        assertEquals(0L, count);
    }

    @Test
    void testGetUserReactionFound() {
        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.of(reaction));

        Reaction result = reactionService.getUserReaction(commentId, userId);

        assertNotNull(result);
        assertEquals(reaction.getId(), result.getId());
        assertEquals(ReactionType.UPVOTE, result.getReactionType());
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
