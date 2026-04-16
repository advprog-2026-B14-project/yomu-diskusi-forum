package id.ac.ui.cs.advprog.yomuforum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReactionController.class)
class ReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReactionService reactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Reaction reaction;
    private UUID reactionId;
    private UUID commentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        reactionId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reaction = new Reaction();
        reaction.setId(reactionId);
        reaction.setCommentId(commentId);
        reaction.setUserId(userId);
        reaction.setReactionType("LIKE");
        reaction.setCreatedAt(new Date());
    }

    @Test
    void testAddReaction() throws Exception {
        when(reactionService.addReaction(any(Reaction.class))).thenReturn(reaction);

        mockMvc.perform(post("/api/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));

        verify(reactionService, times(1)).addReaction(any(Reaction.class));
    }

    @Test
    void testRemoveReaction() throws Exception {
        doNothing().when(reactionService).removeReaction(reactionId);

        mockMvc.perform(delete("/api/reactions/{id}", reactionId))
                .andExpect(status().isNoContent());

        verify(reactionService, times(1)).removeReaction(reactionId);
    }

    @Test
    void testGetReactionsByCommentId() throws Exception {
        Reaction reaction2 = new Reaction();
        reaction2.setId(UUID.randomUUID());
        reaction2.setCommentId(commentId);
        reaction2.setUserId(UUID.randomUUID());
        reaction2.setReactionType("DISLIKE");
        reaction2.setCreatedAt(new Date());

        when(reactionService.getReactionsByCommentId(commentId))
                .thenReturn(Arrays.asList(reaction, reaction2));

        mockMvc.perform(get("/api/reactions/comment/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(reactionService, times(1)).getReactionsByCommentId(commentId);
    }

    @Test
    void testCountReactionsByType() throws Exception {
        when(reactionService.countReactionsByType(commentId, "LIKE")).thenReturn(3L);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/count", commentId)
                        .param("type", "LIKE"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(reactionService, times(1)).countReactionsByType(commentId, "LIKE");
    }

    @Test
    void testGetUserReactionFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(reaction);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }

    @Test
    void testGetUserReactionNotFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(null);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId))
                .andExpect(status().isNotFound());

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }
}
