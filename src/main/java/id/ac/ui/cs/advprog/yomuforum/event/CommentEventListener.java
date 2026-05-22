package id.ac.ui.cs.advprog.yomuforum.event;

/**
 * Observer Pattern – Observer interface.
 * Setiap class yang ingin menerima notifikasi event dari forum
 * harus mengimplementasi interface ini.
 */
public interface CommentEventListener {

    /**
     * Dipanggil ketika sebuah event terjadi.
     * @param event detail event yang terjadi
     */
    void onEvent(CommentEvent event);
}
