package id.ac.ui.cs.advprog.yomuforum.model.observer;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentEventTest {

    @Test
    void testCommentEventGetters() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentEvent event = new CommentEvent(
                EventType.COMMENT_CREATED, commentId, userId, "test details");

        assertEquals(EventType.COMMENT_CREATED, event.getEventType());
        assertEquals(commentId, event.getCommentId());
        assertEquals(userId, event.getUserId());
        assertEquals("test details", event.getDetails());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testCommentEventToString() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentEvent event = new CommentEvent(
                EventType.REACTION_ADDED, commentId, userId, "upvote added");

        String str = event.toString();
        assertTrue(str.contains("REACTION_ADDED"));
        assertTrue(str.contains(commentId.toString()));
        assertTrue(str.contains(userId.toString()));
        assertTrue(str.contains("upvote added"));
    }

    @Test
    void testEventTypeEnum() {
        assertEquals(5, EventType.values().length);
        assertNotNull(EventType.COMMENT_CREATED);
        assertNotNull(EventType.COMMENT_UPDATED);
        assertNotNull(EventType.COMMENT_DELETED);
        assertNotNull(EventType.REACTION_ADDED);
        assertNotNull(EventType.REACTION_REMOVED);
    }
}
