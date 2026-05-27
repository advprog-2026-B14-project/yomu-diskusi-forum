package id.ac.ui.cs.advprog.yomuforum.dto.composite;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CommentComponent {

    UUID getId();
    UUID getUserId();
    UUID getReadingId();
    UUID getParentCommentId();
    String getContent();
    Date getCreatedAt();
    Date getUpdatedAt();

    long getUpvotes();
    void setUpvotes(long upvotes);

    long getDownvotes();
    void setDownvotes(long downvotes);

    List<CommentComponent> getChildren();

    void addChild(CommentComponent child);

    void removeChild(CommentComponent child);

    boolean isLeaf();
}
