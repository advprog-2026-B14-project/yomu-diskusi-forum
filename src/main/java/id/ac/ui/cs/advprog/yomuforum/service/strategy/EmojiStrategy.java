package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class EmojiStrategy implements ReactionStrategy {

    @Override
    public ReactionType getReactionType() {
        return null; 
    }

    public static boolean isEmojiType(ReactionType type) {
        return type != null
                && type != ReactionType.UPVOTE
                && type != ReactionType.DOWNVOTE;
    }

    @Override
    public Reaction apply(Reaction reaction, ReactionRepository repository) {
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
