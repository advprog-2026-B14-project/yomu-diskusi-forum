package id.ac.ui.cs.advprog.yomuforum.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
