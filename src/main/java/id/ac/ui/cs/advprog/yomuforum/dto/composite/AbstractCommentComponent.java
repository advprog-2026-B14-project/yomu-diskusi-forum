package id.ac.ui.cs.advprog.yomuforum.dto.composite;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import java.util.Date;
import java.util.UUID;

public abstract class AbstractCommentComponent implements CommentComponent {

    protected final Comment comment;

    protected AbstractCommentComponent(Comment comment) {
        this.comment = comment;
    }

    @Override public UUID getId()              { return comment.getId(); }
    @Override public UUID getUserId()           { return comment.getUserId(); }
    @Override public UUID getReadingId()        { return comment.getReadingId(); }
    @Override public UUID getParentCommentId()  { return comment.getParentCommentId(); }
    @Override public String getContent()        { return comment.getContent(); }
    @Override public Date getCreatedAt()        { return comment.getCreatedAt(); }
    @Override public Date getUpdatedAt()        { return comment.getUpdatedAt(); }
}
