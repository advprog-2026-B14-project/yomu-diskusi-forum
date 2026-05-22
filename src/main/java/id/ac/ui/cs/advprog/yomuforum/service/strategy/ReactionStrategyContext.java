package id.ac.ui.cs.advprog.yomuforum.service.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern – Context.
 * Menentukan strategy mana yang digunakan berdasarkan ReactionType.
 * Mengumpulkan semua ReactionStrategy beans melalui constructor injection
 * dan memetakan ke masing-masing ReactionType.
 *
 * Prinsip Open-Closed: Menambah jenis reaksi baru cukup membuat class
 * ReactionStrategy baru tanpa mengubah context ini.
 */
@Component
public class ReactionStrategyContext {

    private final Map<ReactionType, ReactionStrategy> strategyMap = new EnumMap<>(ReactionType.class);

    public ReactionStrategyContext(List<ReactionStrategy> strategies) {
        for (ReactionStrategy strategy : strategies) {
            if (strategy instanceof EmojiStrategy) {
                // Register emoji strategy for all emoji types
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

    /**
     * Mengembalikan strategy yang sesuai untuk ReactionType yang diberikan.
     *
     * @param type jenis reaksi
     * @return strategy yang menangani jenis reaksi tersebut
     * @throws IllegalArgumentException jika tidak ada strategy untuk type tersebut
     */
    public ReactionStrategy getStrategy(ReactionType type) {
        ReactionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for reaction type: " + type);
        }
        return strategy;
    }
}
