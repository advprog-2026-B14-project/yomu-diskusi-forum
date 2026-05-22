package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Strategy Pattern – Concrete Strategy (Upvote).
 * Menangani logika upvote:
 * - Exclusive voting: menghapus downvote existing dari user yang sama
 * - Menghapus upvote existing (toggle behavior)
 * - Menyimpan reaksi baru
 */
@Component
public class UpvoteStrategy extends AbstractVoteStrategy {

    @Override
    public ReactionType getReactionType() {
        return ReactionType.UPVOTE;
    }



    @Override
    public int getScoreValue() {
        return 1;
    }
}
