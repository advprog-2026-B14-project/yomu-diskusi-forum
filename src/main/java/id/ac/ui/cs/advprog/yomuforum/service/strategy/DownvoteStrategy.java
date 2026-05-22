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
