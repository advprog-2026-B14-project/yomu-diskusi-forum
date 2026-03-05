package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;

    @Override
    public Reaction addReaction(Reaction reaction) {
        reactionRepository.findByCommentIdAndUserId(reaction.getCommentId(), reaction.getUserId())
                .ifPresent(existingReaction -> {
                    reactionRepository.delete(existingReaction);
                });
        
        reaction.setId(UUID.randomUUID());
        reaction.setCreatedAt(new Date());
        return reactionRepository.save(reaction);
    }

    @Override
    public void removeReaction(UUID id) {
        reactionRepository.deleteById(id);
    }

    @Override
    public List<Reaction> getReactionsByCommentId(UUID commentId) {
        return reactionRepository.findByCommentId(commentId);
    }

    @Override
    public long countReactionsByType(UUID commentId, String reactionType) {
        return reactionRepository.countByCommentIdAndReactionType(commentId, reactionType);
    }

    @Override
    public Reaction getUserReaction(UUID commentId, UUID userId) {
        return reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(null);
    }
}
