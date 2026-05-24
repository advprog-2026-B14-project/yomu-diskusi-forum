package id.ac.ui.cs.advprog.yomuforum.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CommentEventPublisherImpl implements CommentEventPublisher {

    private final List<CommentEventListener> listeners = new CopyOnWriteArrayList<>();

    public CommentEventPublisherImpl(List<CommentEventListener> initialListeners) {
        if (initialListeners != null) {
            this.listeners.addAll(initialListeners);
        }
    }

    @Override
    public void subscribe(CommentEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(CommentEventListener listener) {
        listeners.remove(listener);
    }

    @Async("taskExecutor")
    @Override
    public void notifySubscribers(CommentEvent event) {
        for (CommentEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public int getListenerCount() {
        return listeners.size();
    }
}

