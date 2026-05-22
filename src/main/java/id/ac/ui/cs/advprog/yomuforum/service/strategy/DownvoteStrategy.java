package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Strategy Pattern – Concrete Strategy (Downvote).
 * Menangani logika downvote:
 * - Exclusive voting: menghapus upvote existing dari user yang sama
 * - Menghapus downvote existing (toggle behavior)
 * - Menyimpan reaksi baru
 */
@Component
public class DownvoteStrategy implements ReactionStrategy {

    @Override
    public ReactionType getReactionType() {
        return ReactionType.DOWNVOTE;
    }

    @Override
    public Reaction apply(Reaction reaction, ReactionRepository repository) {
        // Remove any existing vote (upvote or downvote) from this user on this comment
        repository.findByCommentIdAndUserId(reaction.getCommentId(), reaction.getUserId())
                .ifPresent(existing -> {
                    if (existing.getReactionType() == ReactionType.UPVOTE
                            || existing.getReactionType() == ReactionType.DOWNVOTE) {
                        repository.delete(existing);
                    }
                });

        reaction.setId(UUID.randomUUID());
        reaction.setCreatedAt(new Date());
        return repository.save(reaction);
    }

    @Override
    public int getScoreValue() {
        return -1;
    }
}
