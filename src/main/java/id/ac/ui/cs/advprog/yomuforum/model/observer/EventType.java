package id.ac.ui.cs.advprog.yomuforum.model.observer;

/**
 * Observer Pattern – Event types.
 * Enum yang mendefinisikan jenis-jenis event yang bisa terjadi
 * dalam modul diskusi & forum.
 */
public enum EventType {
    COMMENT_CREATED,
    COMMENT_UPDATED,
    COMMENT_DELETED,
    REACTION_ADDED,
    REACTION_REMOVED
}
