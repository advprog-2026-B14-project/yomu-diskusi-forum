package id.ac.ui.cs.advprog.yomuforum.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private String userId;
    private String readingId;
    private String parentCommentId;
}