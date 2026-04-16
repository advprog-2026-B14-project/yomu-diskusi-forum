package id.ac.ui.cs.advprog.yomuforum.repository;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findByCommentId(UUID commentId);
    Optional<Reaction> findByCommentIdAndUserId(UUID commentId, UUID userId);
    long countByCommentIdAndReactionType(UUID commentId, String reactionType);
}
