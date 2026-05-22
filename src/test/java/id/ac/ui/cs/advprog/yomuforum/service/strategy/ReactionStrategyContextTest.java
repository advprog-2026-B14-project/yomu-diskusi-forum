package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReactionStrategyContextTest {

    private ReactionStrategyContext context;

    @BeforeEach
    void setUp() {
        UpvoteStrategy upvote = new UpvoteStrategy();
        DownvoteStrategy downvote = new DownvoteStrategy();
        EmojiStrategy emoji = new EmojiStrategy();

        context = new ReactionStrategyContext(List.of(upvote, downvote, emoji));
    }

    @Test
    void testGetStrategyForUpvote() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.UPVOTE);
        assertNotNull(strategy);
        assertInstanceOf(UpvoteStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForDownvote() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.DOWNVOTE);
        assertNotNull(strategy);
        assertInstanceOf(DownvoteStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForEmojiCelebrate() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.EMOJI_CELEBRATE);
        assertNotNull(strategy);
        assertInstanceOf(EmojiStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForEmojiThumbsUp() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.EMOJI_THUMBS_UP);
        assertNotNull(strategy);
        assertInstanceOf(EmojiStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForEmojiLaugh() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.EMOJI_LAUGH);
        assertNotNull(strategy);
        assertInstanceOf(EmojiStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForEmojiHeart() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.EMOJI_HEART);
        assertNotNull(strategy);
        assertInstanceOf(EmojiStrategy.class, strategy);
    }

    @Test
    void testGetStrategyForEmojiThinking() {
        ReactionStrategy strategy = context.getStrategy(ReactionType.EMOJI_THINKING);
        assertNotNull(strategy);
        assertInstanceOf(EmojiStrategy.class, strategy);
    }

    @Test
    void testAllReactionTypesHaveStrategy() {
        for (ReactionType type : ReactionType.values()) {
            assertDoesNotThrow(() -> context.getStrategy(type),
                    "No strategy found for " + type);
        }
    }

    @Test
    void testConstructorWithEmptyStrategiesAndUnknownTypeThrows() {
        ReactionStrategyContext emptyContext = new ReactionStrategyContext(List.of());

        assertThrows(IllegalArgumentException.class, () -> emptyContext.getStrategy(ReactionType.UPVOTE));
    }

    @Test
    void testConstructorIgnoresStrategyWithoutReactionType() {
        ReactionStrategy nullTypeStrategy = new ReactionStrategy() {
            @Override
            public ReactionType getReactionType() {
                return null;
            }

            @Override
            public id.ac.ui.cs.advprog.yomuforum.model.Reaction apply(
                    id.ac.ui.cs.advprog.yomuforum.model.Reaction reaction,
                    id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository repository) {
                return reaction;
            }

            @Override
            public int getScoreValue() {
                return 0;
            }
        };

        ReactionStrategyContext contextWithNullType = new ReactionStrategyContext(List.of(nullTypeStrategy));

        assertThrows(IllegalArgumentException.class, () -> contextWithNullType.getStrategy(ReactionType.UPVOTE));
    }
}
