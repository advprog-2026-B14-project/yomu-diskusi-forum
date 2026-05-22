package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.exception.ForbiddenException;
import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import id.ac.ui.cs.advprog.yomuforum.exception.ReactionNotFoundException;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.event.CommentEvent;
import id.ac.ui.cs.advprog.yomuforum.event.CommentEventPublisher;
import id.ac.ui.cs.advprog.yomuforum.event.EventType;
import id.ac.ui.cs.advprog.yomuforum.service.strategy.ReactionStrategy;
import id.ac.ui.cs.advprog.yomuforum.service.strategy.ReactionStrategyContext;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final ReactionStrategyContext reactionStrategyContext;
    private final CommentEventPublisher commentEventPublisher;

    @Override
    public Reaction addReaction(Reaction reaction) {
        // Strategy Pattern: delegate to the appropriate strategy based on reaction type
        ReactionStrategy strategy = reactionStrategyContext.getStrategy(reaction.getReactionType());
        Reaction saved = strategy.apply(reaction, reactionRepository);

        // Observer Pattern: notify subscribers about new reaction
        commentEventPublisher.notifySubscribers(
                new CommentEvent(EventType.REACTION_ADDED, saved.getCommentId(), saved.getUserId(),
                        "Reaction added: " + saved.getReactionType()));

        return saved;
    }

    @Override
    public void removeReaction(UUID id, UUID actorUserId, boolean isAdmin) {
        var existing = reactionRepository.findById(id)
                .orElseThrow(() -> new ReactionNotFoundException("Reaction not found"));

        if (actorUserId == null) {
            throw new InvalidInputException("userId is required");
        }

        if (!isAdmin && !existing.getUserId().equals(actorUserId)) {
            throw new ForbiddenException("You can only delete your own reactions unless you are admin");
        }

        reactionRepository.deleteById(id);

        // Observer Pattern: notify subscribers about reaction removal
        commentEventPublisher.notifySubscribers(
                new CommentEvent(EventType.REACTION_REMOVED, existing.getCommentId(), actorUserId,
                        "Reaction removed: " + existing.getReactionType()));
    }

    @Override
    public List<Reaction> getReactionsByCommentId(UUID commentId) {
        return reactionRepository.findByCommentId(commentId);
    }

    @Override
    public long countReactionsByType(UUID commentId, String reactionType) {
        return reactionRepository.countByCommentIdAndReactionType(commentId, ReactionType.from(reactionType));
    }

    @Override
    public Reaction getUserReaction(UUID commentId, UUID userId) {
        return reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(null);
    }
}
