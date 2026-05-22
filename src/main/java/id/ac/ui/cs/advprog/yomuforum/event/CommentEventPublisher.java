package id.ac.ui.cs.advprog.yomuforum.event;

/**
 * Observer Pattern – Subject interface.
 * Mendefinisikan kontrak untuk publisher yang mengelola
 * daftar subscriber dan mengirim notifikasi event.
 */
public interface CommentEventPublisher {

    /**
     * Mendaftarkan listener untuk menerima event.
     */
    void subscribe(CommentEventListener listener);

    /**
     * Menghapus listener dari daftar subscriber.
     */
    void unsubscribe(CommentEventListener listener);

    /**
     * Mengirim notifikasi event ke semua subscriber.
     */
    void notifySubscribers(CommentEvent event);
}
