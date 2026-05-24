package id.ac.ui.cs.advprog.yomuforum.repository;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findByCommentId(UUID commentId);
    Optional<Reaction> findByCommentIdAndUserId(UUID commentId, UUID userId);
    long countByCommentIdAndReactionType(UUID commentId, ReactionType reactionType);

    @org.springframework.data.jpa.repository.Query(
        "SELECT r.commentId, r.reactionType, COUNT(r) " +
        "FROM Reaction r WHERE r.commentId IN :commentIds " +
        "GROUP BY r.commentId, r.reactionType"
    )
    List<Object[]> countReactionsForComments(
        @org.springframework.data.repository.query.Param("commentIds") List<UUID> commentIds
    );
}
