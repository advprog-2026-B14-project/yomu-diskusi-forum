package id.ac.ui.cs.advprog.yomuforum.model.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observer Pattern – Concrete Observer (Notification).
 * Menyimpan notifikasi in-memory ketika event terjadi.
 * Bisa diperluas untuk menyimpan ke database di masa depan.
 */
@Component
public class NotificationCommentEventListener implements CommentEventListener {

    private final List<CommentEvent> notifications = new ArrayList<>();

    @Override
    public void onEvent(CommentEvent event) {
        notifications.add(event);
    }

    /**
     * Mengembalikan daftar semua notifikasi yang sudah diterima.
     * @return unmodifiable list of notifications
     */
    public List<CommentEvent> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    /**
     * Menghapus semua notifikasi yang sudah tersimpan.
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * Mengembalikan jumlah notifikasi yang tersimpan.
     */
    public int getNotificationCount() {
        return notifications.size();
    }
}
