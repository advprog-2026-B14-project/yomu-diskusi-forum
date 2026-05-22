package id.ac.ui.cs.advprog.yomuforum.event;

public interface CommentEventPublisher {

    void subscribe(CommentEventListener listener);

    void unsubscribe(CommentEventListener listener);

    void notifySubscribers(CommentEvent event);
}
