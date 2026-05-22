package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.stereotype.Component;

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
