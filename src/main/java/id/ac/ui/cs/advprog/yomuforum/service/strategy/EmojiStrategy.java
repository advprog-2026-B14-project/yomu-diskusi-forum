package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Strategy Pattern – Concrete Strategy (Emoji).
 * Menangani logika emoji reactions:
 * - Non-exclusive: user bisa punya 1 vote + emoji bersamaan
 * - Mengganti emoji existing dari user yang sama pada komentar yang sama
 * - Menyimpan reaksi baru
 */
@Component
public class EmojiStrategy implements ReactionStrategy {

    /**
     * Emoji strategy menangani semua tipe EMOJI_*.
     * Mengembalikan null karena strategy ini menangani multiple types.
     * Gunakan isEmojiType() untuk mengecek.
     */
    @Override
    public ReactionType getReactionType() {
        return null; // Handles multiple emoji types
    }

    /**
     * Checks if a ReactionType is an emoji type.
     */
    public static boolean isEmojiType(ReactionType type) {
        return type != null
                && type != ReactionType.UPVOTE
                && type != ReactionType.DOWNVOTE;
    }

    @Override
    public Reaction apply(Reaction reaction, ReactionRepository repository) {
        // Remove existing emoji from same user on same comment (replace behavior)
        repository.findByCommentIdAndUserId(reaction.getCommentId(), reaction.getUserId())
                .ifPresent(existing -> {
                    if (isEmojiType(existing.getReactionType())) {
                        repository.delete(existing);
                    }
                });

        reaction.setId(UUID.randomUUID());
        reaction.setCreatedAt(new Date());
        return repository.save(reaction);
    }

    @Override
    public int getScoreValue() {
        return 0;
    }
}
