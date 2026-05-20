package id.ac.ui.cs.advprog.yomuforum.model.observer;

import java.util.Date;
import java.util.UUID;

/**
 * Observer Pattern – Event data class.
 * Membawa informasi tentang event yang terjadi dalam sistem
 * (misalnya komentar dibuat, reaksi ditambahkan).
 */
public class CommentEvent {

    private final EventType eventType;
    private final UUID commentId;
    private final UUID userId;
    private final Date timestamp;
    private final String details;

    public CommentEvent(EventType eventType, UUID commentId, UUID userId, String details) {
        this.eventType = eventType;
        this.commentId = commentId;
        this.userId = userId;
        this.timestamp = new Date();
        this.details = details;
    }

    public EventType getEventType()  { return eventType; }
    public UUID getCommentId()       { return commentId; }
    public UUID getUserId()          { return userId; }
    public Date getTimestamp()        { return timestamp; }
    public String getDetails()       { return details; }

    @Override
    public String toString() {
        return String.format("[%s] commentId=%s userId=%s details=%s",
                eventType, commentId, userId, details);
    }
}
