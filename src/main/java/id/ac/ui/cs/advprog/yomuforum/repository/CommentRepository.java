package id.ac.ui.cs.advprog.yomuforum.repository;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByReadingId(UUID readingId);
    List<Comment> findByParentCommentId(UUID parentCommentId);
    List<Comment> findByUserId(UUID userId);
}
