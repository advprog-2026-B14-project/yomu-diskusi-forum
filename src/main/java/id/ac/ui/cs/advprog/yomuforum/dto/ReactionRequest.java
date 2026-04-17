package id.ac.ui.cs.advprog.yomuforum.dto;

import lombok.Data;

@Data
public class ReactionRequest {
    private String commentId;
    private String userId;
    private String reactionType;
}