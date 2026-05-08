package id.ac.ui.cs.advprog.yomuforum.model;

import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReactionTypeTest {

    @Test
    void testFromUpvote() {
        assertEquals(ReactionType.UPVOTE, ReactionType.from("UPVOTE"));
    }

    @Test
    void testFromDownvoteLowercase() {
        assertEquals(ReactionType.DOWNVOTE, ReactionType.from("downvote"));
    }

    @Test
    void testFromEmojiWithoutPrefix() {
        assertEquals(ReactionType.EMOJI_HEART, ReactionType.from("heart"));
    }

    @Test
    void testFromEmojiWithDash() {
        assertEquals(ReactionType.EMOJI_THUMBS_UP, ReactionType.from("emoji-thumbs-up"));
    }

    @Test
    void testFromEmojiWithPrefixAndLowercase() {
        assertEquals(ReactionType.EMOJI_LAUGH, ReactionType.from("emoji_laugh"));
    }

    @Test
    void testFromNullThrowsInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> ReactionType.from(null));
        assertEquals("reactionType is required", exception.getMessage());
    }

    @Test
    void testFromBlankThrowsInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> ReactionType.from("   "));
        assertEquals("reactionType is required", exception.getMessage());
    }

    @Test
    void testFromInvalidValueThrowsInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> ReactionType.from("invalid"));
        assertEquals("Invalid reactionType: invalid", exception.getMessage());
    }
}
