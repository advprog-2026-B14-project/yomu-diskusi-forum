package id.ac.ui.cs.advprog.yomuforum.model.composite;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Composite Pattern – Composite.
 * Merepresentasikan komentar yang memiliki balasan (children).
 * Mampu menampung child CommentComponent secara rekursif,
 * membentuk tree structure untuk nested comments.
 */
public class CommentComposite implements CommentComponent {

    private final Comment comment;
    private final List<CommentComponent> children = new ArrayList<>();

    public CommentComposite(Comment comment) {
        this.comment = comment;
    }

    @Override public UUID getId()              { return comment.getId(); }
    @Override public UUID getUserId()           { return comment.getUserId(); }
    @Override public UUID getReadingId()        { return comment.getReadingId(); }
    @Override public UUID getParentCommentId()  { return comment.getParentCommentId(); }
    @Override public String getContent()        { return comment.getContent(); }
    @Override public Date getCreatedAt()        { return comment.getCreatedAt(); }
    @Override public Date getUpdatedAt()        { return comment.getUpdatedAt(); }

    @Override
    public List<CommentComponent> getChildren() {
        return children;
    }

    @Override
    public void addChild(CommentComponent child) {
        children.add(child);
    }

    @Override
    public void removeChild(CommentComponent child) {
        children.remove(child);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
