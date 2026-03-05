package id.ac.ui.cs.advprog.yomuforum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.util.Date;

@Getter @Setter
@Entity
@Table(name = "reactions", schema = "public")
public class Reaction {
    @Id
    private UUID id;

    @Column(name = "comment_id", nullable = false) 
    private UUID commentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reaction_type", nullable = false)
    private String reactionType;

    @Column(name = "created_at", updatable = false)
    private Date createdAt = new Date();
}