package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;

public interface ReactionStrategy {

    ReactionType getReactionType();

    Reaction apply(Reaction reaction, ReactionRepository repository);

    int getScoreValue();
}
