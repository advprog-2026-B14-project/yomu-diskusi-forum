package id.ac.ui.cs.advprog.yomuforum.dto.composite;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentComposite extends AbstractCommentComponent {

    public CommentComposite(Comment comment) {
        super(comment);
    }

    private final List<CommentComponent> children = new ArrayList<>();
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
