package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractVoteStrategy implements ReactionStrategy {

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
}
