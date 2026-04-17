package id.ac.ui.cs.advprog.yomuforum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.util.Date;

@Getter @Setter
@Entity
@Table(name = "comments", schema = "public") 
public class Comment {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reading_id", nullable = false)
    private UUID readingId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", updatable = false)
    private Date createdAt = new Date();

    @Column(name = "updated_at")
    private Date updatedAt = new Date();
}