package id.ac.ui.cs.advprog.yomuforum.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern – Concrete Observer (Logging).
 * Menerima event dari publisher dan mencatat ke log menggunakan SLF4J.
 * Contoh implementasi observer untuk audit trail / debugging.
 */
@Component
public class LoggingCommentEventListener implements CommentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingCommentEventListener.class);

    @Override
    public void onEvent(CommentEvent event) {
        logger.info("Forum Event: [{}] commentId={} userId={} details={}",
                event.getEventType(),
                event.getCommentId(),
                event.getUserId(),
                event.getDetails());
    }
}
