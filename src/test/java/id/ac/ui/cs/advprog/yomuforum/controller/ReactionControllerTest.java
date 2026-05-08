package id.ac.ui.cs.advprog.yomuforum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomuforum.config.SecurityConfig;
import id.ac.ui.cs.advprog.yomuforum.dto.ReactionRequest;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.model.ReactionType;
import id.ac.ui.cs.advprog.yomuforum.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReactionController.class)
@Import({SecurityConfig.class, ReactionControllerTest.TestSecurityUsers.class})
class ReactionControllerTest {

    static final String TEST_USER = "test-user";
    static final String PASSWORD = "password";

    @TestConfiguration
    static class TestSecurityUsers {
        @Bean
        public UserDetailsService userDetailsService() {
            var user = User.builder()
                    .username(TEST_USER)
                    .password("{noop}" + PASSWORD)
                    .roles("USER")
                    .build();
            return new InMemoryUserDetailsManager(user);
        }
    }

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
        reaction.setReactionType(ReactionType.UPVOTE);
        reaction.setCreatedAt(new Date());
    }

    @Test
    void testAddReaction() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setUserId(userId.toString());
        request.setReactionType("UPVOTE");

        when(reactionService.addReaction(any(Reaction.class))).thenReturn(reaction);

        mockMvc.perform(post("/api/reactions")
                        .with(httpBasic(TEST_USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("UPVOTE"));

        verify(reactionService, times(1)).addReaction(any(Reaction.class));
    }

    @Test
    void testRemoveReaction() throws Exception {
        doNothing().when(reactionService).removeReaction(reactionId);

        mockMvc.perform(delete("/api/reactions/{id}", reactionId)
                        .with(httpBasic(TEST_USER, PASSWORD)))
                .andExpect(status().isNoContent());

        verify(reactionService, times(1)).removeReaction(reactionId);
    }

    @Test
    void testGetReactionsByCommentId() throws Exception {
        Reaction reaction2 = new Reaction();
        reaction2.setId(UUID.randomUUID());
        reaction2.setCommentId(commentId);
        reaction2.setUserId(UUID.randomUUID());
        reaction2.setReactionType(ReactionType.DOWNVOTE);
        reaction2.setCreatedAt(new Date());

        when(reactionService.getReactionsByCommentId(commentId))
                .thenReturn(Arrays.asList(reaction, reaction2));

        mockMvc.perform(get("/api/reactions/comment/{commentId}", commentId)
                        .with(httpBasic(TEST_USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(reactionService, times(1)).getReactionsByCommentId(commentId);
    }

    @Test
    void testCountReactionsByType() throws Exception {
        when(reactionService.countReactionsByType(commentId, "UPVOTE")).thenReturn(3L);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/count", commentId)
                        .param("type", "UPVOTE")
                        .with(httpBasic(TEST_USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(reactionService, times(1)).countReactionsByType(commentId, "UPVOTE");
    }

    @Test
    void testGetUserReactionFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(reaction);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId)
                        .with(httpBasic(TEST_USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("UPVOTE"));

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }

    @Test
    void testGetUserReactionNotFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(null);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId)
                        .with(httpBasic(TEST_USER, PASSWORD)))
                .andExpect(status().isNotFound());

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }
}
