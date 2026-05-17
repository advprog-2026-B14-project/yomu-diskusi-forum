package id.ac.ui.cs.advprog.yomuforum.model.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationCommentEventListenerTest {

    private NotificationCommentEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationCommentEventListener();
    }

    @Test
    void testOnEventStoresNotification() {
        CommentEvent event = new CommentEvent(
                EventType.COMMENT_CREATED, UUID.randomUUID(), UUID.randomUUID(), "new comment");

        listener.onEvent(event);

        assertEquals(1, listener.getNotificationCount());
        assertEquals(event, listener.getNotifications().get(0));
    }

    @Test
    void testMultipleEventsStored() {
        CommentEvent event1 = new CommentEvent(
                EventType.COMMENT_CREATED, UUID.randomUUID(), UUID.randomUUID(), "first");
        CommentEvent event2 = new CommentEvent(
                EventType.REACTION_ADDED, UUID.randomUUID(), UUID.randomUUID(), "second");
        CommentEvent event3 = new CommentEvent(
                EventType.COMMENT_DELETED, UUID.randomUUID(), UUID.randomUUID(), "third");

        listener.onEvent(event1);
        listener.onEvent(event2);
        listener.onEvent(event3);

        assertEquals(3, listener.getNotificationCount());
        assertEquals(EventType.COMMENT_CREATED, listener.getNotifications().get(0).getEventType());
        assertEquals(EventType.REACTION_ADDED, listener.getNotifications().get(1).getEventType());
        assertEquals(EventType.COMMENT_DELETED, listener.getNotifications().get(2).getEventType());
    }

    @Test
    void testClearNotifications() {
        listener.onEvent(new CommentEvent(
                EventType.COMMENT_CREATED, UUID.randomUUID(), UUID.randomUUID(), "test"));
        assertEquals(1, listener.getNotificationCount());

        listener.clearNotifications();
        assertEquals(0, listener.getNotificationCount());
        assertTrue(listener.getNotifications().isEmpty());
    }

    @Test
    void testGetNotificationsReturnsUnmodifiableList() {
        listener.onEvent(new CommentEvent(
                EventType.COMMENT_CREATED, UUID.randomUUID(), UUID.randomUUID(), "test"));

        assertThrows(UnsupportedOperationException.class, () ->
                listener.getNotifications().add(new CommentEvent(
                        EventType.COMMENT_DELETED, UUID.randomUUID(), UUID.randomUUID(), "x")));
    }

    @Test
    void testInitiallyEmpty() {
        assertEquals(0, listener.getNotificationCount());
        assertTrue(listener.getNotifications().isEmpty());
    }
}
