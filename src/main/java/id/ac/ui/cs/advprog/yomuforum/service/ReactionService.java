package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;

import java.util.List;
import java.util.UUID;

public interface ReactionService {
    Reaction addReaction(Reaction reaction);
    void removeReaction(UUID id);
    List<Reaction> getReactionsByCommentId(UUID commentId);
    long countReactionsByType(UUID commentId, String reactionType);
    Reaction getUserReaction(UUID commentId, UUID userId);
}
