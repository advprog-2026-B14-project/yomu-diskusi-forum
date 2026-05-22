package id.ac.ui.cs.advprog.yomuforum.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer Pattern – Concrete Subject (Async).
 * Mengelola daftar listener dan mengirim notifikasi ke semua
 * listener yang terdaftar ketika sebuah event terjadi.
 *
 * Notifikasi dikirim secara ASYNCHRONOUS di thread terpisah
 * agar tidak memblokir response ke client.
 *
 * Menggunakan CopyOnWriteArrayList untuk thread-safety
 * saat subscribe/unsubscribe bersamaan dengan notifikasi.
 */
@Component
public class CommentEventPublisherImpl implements CommentEventPublisher {

    private final List<CommentEventListener> listeners = new CopyOnWriteArrayList<>();

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

    /**
     * Mengirim notifikasi ke semua subscriber secara ASYNCHRONOUS.
     * Method ini berjalan di thread pool "taskExecutor" (lihat AsyncConfig),
     * sehingga caller (service layer) tidak perlu menunggu semua listener
     * selesai diproses sebelum mengembalikan response ke client.
     */
    @Async("taskExecutor")
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

