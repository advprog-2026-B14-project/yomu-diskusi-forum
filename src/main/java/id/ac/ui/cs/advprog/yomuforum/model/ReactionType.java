package id.ac.ui.cs.advprog.yomuforum.model;

import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;

import java.util.Locale;

public enum ReactionType {
    UPVOTE,
    DOWNVOTE,
    EMOJI_CELEBRATE,
    EMOJI_THUMBS_UP,
    EMOJI_LAUGH,
    EMOJI_HEART,
    EMOJI_THINKING;

    public static ReactionType from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("reactionType is required");
        }

        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);

        if (!normalized.startsWith("EMOJI_") && !normalized.equals("UPVOTE") && !normalized.equals("DOWNVOTE")) {
            normalized = "EMOJI_" + normalized;
        }

        try {
            return ReactionType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Invalid reactionType: " + value);
        }
    }
}