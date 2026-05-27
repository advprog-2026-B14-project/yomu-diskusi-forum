package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.stereotype.Component;

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
