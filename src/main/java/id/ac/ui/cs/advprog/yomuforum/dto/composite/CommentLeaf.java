package id.ac.ui.cs.advprog.yomuforum.dto.composite;

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
public class CommentLeaf extends AbstractCommentComponent {

    public CommentLeaf(Comment comment) {
        super(comment);
    }

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
