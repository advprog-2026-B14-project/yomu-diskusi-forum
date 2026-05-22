package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern – Concrete Strategy (Downvote).
 * Menangani logika downvote:
 * - Exclusive voting: menghapus upvote existing dari user yang sama
 * - Menghapus downvote existing (toggle behavior)
 * - Menyimpan reaksi baru
 */
@Component
public class DownvoteStrategy extends AbstractVoteStrategy {

    @Override
    public ReactionType getReactionType() {
        return ReactionType.DOWNVOTE;
    }



    @Override
    public int getScoreValue() {
        return -1;
    }
}
