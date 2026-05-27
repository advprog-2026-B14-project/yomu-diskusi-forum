package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionStrategyTest {

    @Mock
    private ReactionRepository reactionRepository;

    private UpvoteStrategy upvoteStrategy;
    private DownvoteStrategy downvoteStrategy;
    private EmojiStrategy emojiStrategy;

    private UUID commentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        upvoteStrategy = new UpvoteStrategy();
        downvoteStrategy = new DownvoteStrategy();
        emojiStrategy = new EmojiStrategy();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    private Reaction createReaction(ReactionType type) {
        Reaction r = new Reaction();
        r.setCommentId(commentId);
        r.setUserId(userId);
        r.setReactionType(type);
        return r;
    }

    // ─── UpvoteStrategy tests ──────────────────────────────────────

    @Test
    void testUpvoteStrategyGetReactionType() {
        assertEquals(ReactionType.UPVOTE, upvoteStrategy.getReactionType());
    }

    @Test
    void testUpvoteStrategyGetScoreValue() {
        assertEquals(1, upvoteStrategy.getScoreValue());
    }

    @Test
    void testUpvoteStrategyApplyNoExistingReaction() {
        Reaction reaction = createReaction(ReactionType.UPVOTE);

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        Reaction result = upvoteStrategy.apply(reaction, reactionRepository);

        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        verify(reactionRepository).save(any(Reaction.class));
        verify(reactionRepository, never()).delete(any());
    }

    @Test
    void testUpvoteStrategyRemovesExistingDownvote() {
        Reaction reaction = createReaction(ReactionType.UPVOTE);
        Reaction existingDownvote = createReaction(ReactionType.DOWNVOTE);
        existingDownvote.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingDownvote));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        upvoteStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository).delete(existingDownvote);
        verify(reactionRepository).save(any(Reaction.class));
    }

    @Test
    void testUpvoteStrategyRemovesExistingUpvote() {
        Reaction reaction = createReaction(ReactionType.UPVOTE);
        Reaction existingUpvote = createReaction(ReactionType.UPVOTE);
        existingUpvote.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingUpvote));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        upvoteStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository).delete(existingUpvote);
    }

    @Test
    void testUpvoteStrategyDoesNotRemoveExistingEmoji() {
        Reaction reaction = createReaction(ReactionType.UPVOTE);
        Reaction existingEmoji = createReaction(ReactionType.EMOJI_HEART);
        existingEmoji.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingEmoji));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        upvoteStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository, never()).delete(any());
    }

    // ─── DownvoteStrategy tests ────────────────────────────────────

    @Test
    void testDownvoteStrategyGetReactionType() {
        assertEquals(ReactionType.DOWNVOTE, downvoteStrategy.getReactionType());
    }

    @Test
    void testDownvoteStrategyGetScoreValue() {
        assertEquals(-1, downvoteStrategy.getScoreValue());
    }

    @Test
    void testDownvoteStrategyApplyNoExistingReaction() {
        Reaction reaction = createReaction(ReactionType.DOWNVOTE);

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        Reaction result = downvoteStrategy.apply(reaction, reactionRepository);

        assertNotNull(result.getId());
        verify(reactionRepository).save(any(Reaction.class));
        verify(reactionRepository, never()).delete(any());
    }

    @Test
    void testDownvoteStrategyRemovesExistingUpvote() {
        Reaction reaction = createReaction(ReactionType.DOWNVOTE);
        Reaction existingUpvote = createReaction(ReactionType.UPVOTE);
        existingUpvote.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingUpvote));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        downvoteStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository).delete(existingUpvote);
    }

    @Test
    void testDownvoteStrategyDoesNotRemoveExistingEmoji() {
        Reaction reaction = createReaction(ReactionType.DOWNVOTE);
        Reaction existingEmoji = createReaction(ReactionType.EMOJI_LAUGH);
        existingEmoji.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingEmoji));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        downvoteStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository, never()).delete(any());
    }

    // ─── EmojiStrategy tests ──────────────────────────────────────

    @Test
    void testEmojiStrategyGetReactionTypeIsNull() {
        assertNull(emojiStrategy.getReactionType());
    }

    @Test
    void testEmojiStrategyGetScoreValue() {
        assertEquals(0, emojiStrategy.getScoreValue());
    }

    @Test
    void testEmojiStrategyIsEmojiType() {
        assertTrue(EmojiStrategy.isEmojiType(ReactionType.EMOJI_CELEBRATE));
        assertTrue(EmojiStrategy.isEmojiType(ReactionType.EMOJI_THUMBS_UP));
        assertTrue(EmojiStrategy.isEmojiType(ReactionType.EMOJI_LAUGH));
        assertTrue(EmojiStrategy.isEmojiType(ReactionType.EMOJI_HEART));
        assertTrue(EmojiStrategy.isEmojiType(ReactionType.EMOJI_THINKING));
        assertFalse(EmojiStrategy.isEmojiType(ReactionType.UPVOTE));
        assertFalse(EmojiStrategy.isEmojiType(ReactionType.DOWNVOTE));
        assertFalse(EmojiStrategy.isEmojiType(null));
    }

    @Test
    void testEmojiStrategyApplyNoExistingReaction() {
        Reaction reaction = createReaction(ReactionType.EMOJI_HEART);

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        Reaction result = emojiStrategy.apply(reaction, reactionRepository);

        assertNotNull(result.getId());
        verify(reactionRepository).save(any(Reaction.class));
        verify(reactionRepository, never()).delete(any());
    }

    @Test
    void testEmojiStrategyRemovesExistingEmoji() {
        Reaction reaction = createReaction(ReactionType.EMOJI_HEART);
        Reaction existingEmoji = createReaction(ReactionType.EMOJI_LAUGH);
        existingEmoji.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingEmoji));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        emojiStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository).delete(existingEmoji);
    }

    @Test
    void testEmojiStrategyDoesNotRemoveExistingVote() {
        Reaction reaction = createReaction(ReactionType.EMOJI_HEART);
        Reaction existingUpvote = createReaction(ReactionType.UPVOTE);
        existingUpvote.setId(UUID.randomUUID());

        when(reactionRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(List.of(existingUpvote));
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(i -> i.getArgument(0));

        emojiStrategy.apply(reaction, reactionRepository);

        verify(reactionRepository, never()).delete(any());
    }
}
