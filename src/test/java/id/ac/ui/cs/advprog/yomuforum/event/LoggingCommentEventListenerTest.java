package id.ac.ui.cs.advprog.yomuforum.event;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoggingCommentEventListenerTest {

    @Test
    void testOnEventLogsMessage() {
        LoggingCommentEventListener listener = new LoggingCommentEventListener();

        // Set up logback list appender to capture log output
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingCommentEventListener.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentEvent event = new CommentEvent(
                EventType.COMMENT_CREATED, commentId, userId, "test comment created");

        listener.onEvent(event);

        // Verify log message was captured
        assertFalse(listAppender.list.isEmpty());
        String logMessage = listAppender.list.get(0).getFormattedMessage();
        assertTrue(logMessage.contains("COMMENT_CREATED"));
        assertTrue(logMessage.contains(commentId.toString()));
        assertTrue(logMessage.contains(userId.toString()));
        assertTrue(logMessage.contains("test comment created"));

        logger.detachAppender(listAppender);
    }

    @Test
    void testOnEventWithReactionEvent() {
        LoggingCommentEventListener listener = new LoggingCommentEventListener();

        Logger logger = (Logger) LoggerFactory.getLogger(LoggingCommentEventListener.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        CommentEvent event = new CommentEvent(
                EventType.REACTION_ADDED, UUID.randomUUID(), UUID.randomUUID(), "upvote");

        listener.onEvent(event);

        assertFalse(listAppender.list.isEmpty());
        String logMessage = listAppender.list.get(0).getFormattedMessage();
        assertTrue(logMessage.contains("REACTION_ADDED"));

        logger.detachAppender(listAppender);
    }
}
