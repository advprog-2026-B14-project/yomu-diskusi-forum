package id.ac.ui.cs.advprog.yomuforum.model.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern – Concrete Subject.
 * Mengelola daftar listener dan mengirim notifikasi ke semua
 * listener yang terdaftar ketika sebuah event terjadi.
 *
 * Spring akan otomatis menginjeksi semua bean yang implements
 * CommentEventListener melalui constructor injection.
 */
@Component
public class CommentEventPublisherImpl implements CommentEventPublisher {

    private final List<CommentEventListener> listeners = new ArrayList<>();

    /**
     * Constructor injection: Spring secara otomatis memasukkan semua bean
     * yang implement CommentEventListener. Jika tidak ada, list kosong digunakan.
     */
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

    @Override
    public void notifySubscribers(CommentEvent event) {
        for (CommentEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /** Visible for testing. */
    public int getListenerCount() {
        return listeners.size();
    }
}
