package id.ac.ui.cs.advprog.yomuforum.model.strategy;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;

/**
 * Strategy Pattern – Strategy interface.
 * Mendefinisikan kontrak untuk menangani berbagai jenis reaksi.
 * Setiap implementasi mengenkapsulasi logika spesifik per jenis reaksi
 * (Upvote, Downvote, Emoji), menggantikan if-else chain yang melanggar OCP.
 */
public interface ReactionStrategy {

    /**
     * Mengembalikan jenis ReactionType yang ditangani oleh strategy ini.
     */
    ReactionType getReactionType();

    /**
     * Mengeksekusi logika penanganan reaksi.
     * Termasuk penghapusan reaksi yang konflik (misal upvote hapus downvote).
     *
     * @param reaction reaksi baru yang akan ditambahkan
     * @param repository akses ke data layer
     * @return reaksi yang sudah disimpan
     */
    Reaction apply(Reaction reaction, ReactionRepository repository);

    /**
     * Mengembalikan nilai skor dari reaksi ini.
     * Upvote = +1, Downvote = -1, Emoji = 0.
     */
    int getScoreValue();
}
