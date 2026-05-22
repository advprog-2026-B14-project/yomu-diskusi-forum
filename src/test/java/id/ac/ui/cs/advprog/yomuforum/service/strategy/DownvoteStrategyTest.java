package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DownvoteStrategyTest {

    private DownvoteStrategy strategy;
    private ReactionRepository repository;
    private Reaction reaction;

    @BeforeEach
    void setUp() {
        strategy = new DownvoteStrategy();
        repository = mock(ReactionRepository.class);

        reaction = new Reaction();
        reaction.setCommentId(UUID.randomUUID());
        reaction.setUserId(UUID.randomUUID());
        reaction.setReactionType(ReactionType.DOWNVOTE);
    }

    @Test
    void testGetReactionTypeAndScoreValue() {
        assertEquals(ReactionType.DOWNVOTE, strategy.getReactionType());
        assertEquals(-1, strategy.getScoreValue());
    }

    @Test
    void testApplyDeletesExistingDownvoteAndSavesNewReaction() {
        Reaction existing = new Reaction();
        existing.setId(UUID.randomUUID());
        existing.setCommentId(reaction.getCommentId());
        existing.setUserId(reaction.getUserId());
        existing.setReactionType(ReactionType.DOWNVOTE);

        when(repository.findByCommentIdAndUserId(reaction.getCommentId(), reaction.getUserId()))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Reaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reaction result = strategy.apply(reaction, repository);

        assertNotNull(result.getId());
        assertEquals(ReactionType.DOWNVOTE, result.getReactionType());
        verify(repository).delete(existing);
        verify(repository).save(reaction);
    }

    @Test
    void testApplyKeepsNonVoteReactionAndSavesNewReaction() {
        Reaction existing = new Reaction();
        existing.setId(UUID.randomUUID());
        existing.setCommentId(reaction.getCommentId());
        existing.setUserId(reaction.getUserId());
        existing.setReactionType(ReactionType.EMOJI_HEART);

        when(repository.findByCommentIdAndUserId(reaction.getCommentId(), reaction.getUserId()))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Reaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        strategy.apply(reaction, repository);

        verify(repository, never()).delete(any());
        verify(repository).save(reaction);
    }
}
