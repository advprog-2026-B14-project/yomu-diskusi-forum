package id.ac.ui.cs.advprog.yomuforum.event;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NotificationCommentEventListener implements CommentEventListener {

    private final List<CommentEvent> notifications = new ArrayList<>();

    @Override
    public void onEvent(CommentEvent event) {
        notifications.add(event);
    }

    public List<CommentEvent> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    public int getNotificationCount() {
        return notifications.size();
    }
}
