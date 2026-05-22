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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReactionController.class)
@Import(SecurityConfig.class)
class ReactionControllerTest {

    static final String TEST_USER = "00000000-0000-0000-0000-000000000001";
    static final String INVALID_USER = "not-a-uuid";

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
        request.setReactionType("UPVOTE");

        when(reactionService.addReaction(any(Reaction.class))).thenReturn(reaction);

        mockMvc.perform(post("/api/reactions")
                        .header("X-User-Id", TEST_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("UPVOTE"));

        verify(reactionService, times(1)).addReaction(any(Reaction.class));
    }

    @Test
    void testAddReactionWithInvalidUserId() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        mockMvc.perform(post("/api/reactions")
                        .header("X-User-Id", INVALID_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid UUID format: not-a-uuid"));

        verify(reactionService, never()).addReaction(any(Reaction.class));
    }

    @Test
    void testAddReactionWithoutUserId() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId(commentId.toString());
        request.setReactionType("UPVOTE");

        mockMvc.perform(post("/api/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reactionService, never()).addReaction(any(Reaction.class));
    }

    @Test
    void testRemoveReaction() throws Exception {
        doNothing().when(reactionService).removeReaction(eq(reactionId), any(UUID.class), eq(false));

        mockMvc.perform(delete("/api/reactions/{id}", reactionId)
                .header("X-User-Id", TEST_USER))
            .andExpect(status().isNoContent());

        verify(reactionService, times(1)).removeReaction(eq(reactionId), any(UUID.class), eq(false));
    }

    @Test
    void testRemoveReactionWithInvalidUserId() throws Exception {
        mockMvc.perform(delete("/api/reactions/{id}", reactionId)
                        .header("X-User-Id", INVALID_USER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid UUID format: not-a-uuid"));

        verify(reactionService, never()).removeReaction(any(UUID.class), any(UUID.class), anyBoolean());
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

        mockMvc.perform(get("/api/reactions/comment/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(reactionService, times(1)).getReactionsByCommentId(commentId);
    }

    @Test
    void testCountReactionsByType() throws Exception {
        when(reactionService.countReactionsByType(commentId, "UPVOTE")).thenReturn(3L);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/count", commentId)
                        .param("type", "UPVOTE"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(reactionService, times(1)).countReactionsByType(commentId, "UPVOTE");
    }

    @Test
    void testGetUserReactionFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(reaction);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reactionId.toString()))
                .andExpect(jsonPath("$.reactionType").value("UPVOTE"));

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }

    @Test
    void testGetUserReactionNotFound() throws Exception {
        when(reactionService.getUserReaction(commentId, userId)).thenReturn(null);

        mockMvc.perform(get("/api/reactions/comment/{commentId}/user/{userId}", commentId, userId))
                .andExpect(status().isNotFound());

        verify(reactionService, times(1)).getUserReaction(commentId, userId);
    }

    @Test
    void testPrivateHelpers() {
        ReactionController controller = new ReactionController(reactionService);

        assertNull(ReflectionTestUtils.invokeMethod(controller, "parseUuid", (String) null));
        assertNull(ReflectionTestUtils.invokeMethod(controller, "parseUuid", "   "));
        assertEquals(UUID.fromString(TEST_USER), ReflectionTestUtils.invokeMethod(controller, "parseUuid", TEST_USER));
        assertThrows(Exception.class, () -> ReflectionTestUtils.invokeMethod(controller, "parseUuid", INVALID_USER));

        assertThrows(Exception.class, () -> ReflectionTestUtils.invokeMethod(controller, "parseRequiredUuid", "", "userId"));
        assertEquals(UUID.fromString(TEST_USER), ReflectionTestUtils.invokeMethod(controller, "parseRequiredUuid", TEST_USER, "userId"));

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(controller, "isAdmin", "ADMIN"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(controller, "isAdmin", "USER"));

        assertEquals(TEST_USER, ReflectionTestUtils.invokeMethod(controller, "resolveUserId", TEST_USER, "fallback"));
        assertEquals("fallback", ReflectionTestUtils.invokeMethod(controller, "resolveUserId", "", "fallback"));
    }

    @Test
    void testAddReactionWhenDisabled() throws Exception {
        ReflectionTestUtils.setField(mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(ReactionController.class), "isReactionEnabled", false);
        try {
            ReactionRequest request = new ReactionRequest();
            request.setCommentId(commentId.toString());
            request.setUserId(TEST_USER);
            request.setReactionType("UPVOTE");

            mockMvc.perform(post("/api/reactions")
                            .header("X-User-Id", TEST_USER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isServiceUnavailable());
        } finally {
            ReflectionTestUtils.setField(mockMvc.getDispatcherServlet().getWebApplicationContext().getBean(ReactionController.class), "isReactionEnabled", true);
        }
    }
}

