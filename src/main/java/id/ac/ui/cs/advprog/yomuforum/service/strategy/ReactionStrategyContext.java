package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ReactionStrategyContext {

    private final Map<ReactionType, ReactionStrategy> strategyMap = new EnumMap<>(ReactionType.class);

    public ReactionStrategyContext(List<ReactionStrategy> strategies) {
        for (ReactionStrategy strategy : strategies) {
            if (strategy instanceof EmojiStrategy) {
                for (ReactionType type : ReactionType.values()) {
                    if (EmojiStrategy.isEmojiType(type)) {
                        strategyMap.put(type, strategy);
                    }
                }
            } else if (strategy.getReactionType() != null) {
                strategyMap.put(strategy.getReactionType(), strategy);
            }
        }
    }

    public ReactionStrategy getStrategy(ReactionType type) {
        ReactionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for reaction type: " + type);
        }
        return strategy;
    }
}
