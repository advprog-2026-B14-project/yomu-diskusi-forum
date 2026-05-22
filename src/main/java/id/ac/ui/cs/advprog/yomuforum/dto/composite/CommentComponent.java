package id.ac.ui.cs.advprog.yomuforum.dto.composite;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Composite Pattern – Component interface.
 * Mendefinisikan interface seragam untuk komentar induk (Composite)
 * dan komentar tanpa balasan (Leaf), sehingga keduanya bisa
 * diperlakukan dengan cara yang sama saat membangun tree komentar.
 */
public interface CommentComponent {

    UUID getId();
    UUID getUserId();
    UUID getReadingId();
    UUID getParentCommentId();
    String getContent();
    Date getCreatedAt();
    Date getUpdatedAt();

    /** Returns child comments. Leaf returns empty list. */
    List<CommentComponent> getChildren();

    /** Adds a child comment. Leaf throws UnsupportedOperationException. */
    void addChild(CommentComponent child);

    /** Removes a child comment. Leaf throws UnsupportedOperationException. */
    void removeChild(CommentComponent child);

    /** Returns true if this is a leaf node (no children allowed). */
    boolean isLeaf();
}
