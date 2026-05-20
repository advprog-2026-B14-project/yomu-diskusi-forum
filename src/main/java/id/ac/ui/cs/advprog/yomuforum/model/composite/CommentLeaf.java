package id.ac.ui.cs.advprog.yomuforum.model.composite;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Composite Pattern – Leaf.
 * Merepresentasikan komentar yang tidak memiliki balasan (child).
 * addChild() dan removeChild() melempar UnsupportedOperationException
 * karena leaf tidak boleh memiliki anak.
 */
public class CommentLeaf implements CommentComponent {

    private final Comment comment;

    public CommentLeaf(Comment comment) {
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
        return Collections.emptyList();
    }

    @Override
    public void addChild(CommentComponent child) {
        throw new UnsupportedOperationException("Leaf node cannot have children");
    }

    @Override
    public void removeChild(CommentComponent child) {
        throw new UnsupportedOperationException("Leaf node cannot have children");
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
