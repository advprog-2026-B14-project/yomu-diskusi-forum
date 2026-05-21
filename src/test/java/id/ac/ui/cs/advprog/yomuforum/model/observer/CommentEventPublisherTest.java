package id.ac.ui.cs.advprog.yomuforum.model.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentEventPublisherTest {

    private CommentEventPublisherImpl publisher;

    @BeforeEach
    void setUp() {
        publisher = new CommentEventPublisherImpl(new ArrayList<>());
    }

    @Test
    void testSubscribeAddsListener() {
        CommentEventListener listener = event -> { };
        publisher.subscribe(listener);
        assertEquals(1, publisher.getListenerCount());
    }

    @Test
    void testSubscribeNullDoesNothing() {
        publisher.subscribe(null);
        assertEquals(0, publisher.getListenerCount());
    }

    @Test
    void testSubscribeDuplicateDoesNothing() {
        CommentEventListener listener = event -> { };
        publisher.subscribe(listener);
        publisher.subscribe(listener);
        assertEquals(1, publisher.getListenerCount());
    }

    @Test
    void testUnsubscribeRemovesListener() {
        CommentEventListener listener = event -> { };
        publisher.subscribe(listener);
        assertEquals(1, publisher.getListenerCount());

        publisher.unsubscribe(listener);
        assertEquals(0, publisher.getListenerCount());
    }

    @Test
    void testUnsubscribeNonExistentDoesNothing() {
        CommentEventListener listener = event -> { };
        publisher.unsubscribe(listener);
        assertEquals(0, publisher.getListenerCount());
    }

    @Test
    void testNotifySubscribersCallsAllListeners() {
        List<CommentEvent> received1 = new ArrayList<>();
        List<CommentEvent> received2 = new ArrayList<>();

        publisher.subscribe(received1::add);
        publisher.subscribe(received2::add);

        CommentEvent event = new CommentEvent(
                EventType.COMMENT_CREATED, UUID.randomUUID(), UUID.randomUUID(), "test");

        publisher.notifySubscribers(event);

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
        assertEquals(event, received1.get(0));
        assertEquals(event, received2.get(0));
    }

    @Test
    void testNotifyAfterUnsubscribeDoesNotCallRemovedListener() {
        List<CommentEvent> received = new ArrayList<>();
        CommentEventListener listener = received::add;

        publisher.subscribe(listener);
        publisher.unsubscribe(listener);

        CommentEvent event = new CommentEvent(
                EventType.COMMENT_DELETED, UUID.randomUUID(), UUID.randomUUID(), "test");
        publisher.notifySubscribers(event);

        assertTrue(received.isEmpty());
    }

    @Test
    void testConstructorWithInitialListeners() {
        List<CommentEventListener> initial = new ArrayList<>();
        initial.add(event -> { });
        initial.add(event -> { });

        CommentEventPublisherImpl pub = new CommentEventPublisherImpl(initial);
        assertEquals(2, pub.getListenerCount());
    }

    @Test
    void testConstructorWithNullListeners() {
        CommentEventPublisherImpl pub = new CommentEventPublisherImpl(null);
        assertEquals(0, pub.getListenerCount());
    }
}
