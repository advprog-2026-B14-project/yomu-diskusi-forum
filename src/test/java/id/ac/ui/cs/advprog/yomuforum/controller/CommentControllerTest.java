package id.ac.ui.cs.advprog.yomuforum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.service.CommentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Comment comment;
    private UUID commentId;
    private UUID userId;
    private UUID readingId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        readingId = UUID.randomUUID();

        comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setReadingId(readingId);
        comment.setContent("Test comment");
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
    }

    @Test
    void testCreateComment() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setUserId(userId.toString());
        request.setReadingId(readingId.toString());
        request.setParentCommentId(UUID.randomUUID().toString());

        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));

        verify(commentService, times(1)).createComment(any(Comment.class));
    }

    @Test
    void testCreateCommentWithoutParentCommentId() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");
        request.setUserId(userId.toString());
        request.setReadingId(readingId.toString());

        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));

        verify(commentService, times(1)).createComment(any(Comment.class));
    }

    @Test
    void testUpdateComment() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setContent("Updated comment");
        request.setUserId(userId.toString());
        request.setReadingId(readingId.toString());

        Comment updatedComment = new Comment();
        updatedComment.setId(commentId);
        updatedComment.setContent("Updated comment");
        updatedComment.setUserId(userId);
        updatedComment.setReadingId(readingId);
        updatedComment.setCreatedAt(new Date());
        updatedComment.setUpdatedAt(new Date());

        when(commentService.updateComment(eq(commentId), any(Comment.class))).thenReturn(updatedComment);

        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));

        verify(commentService, times(1)).updateComment(eq(commentId), any(Comment.class));
    }

    @Test
    void testDeleteComment() throws Exception {
        doNothing().when(commentService).deleteComment(commentId);

        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    void testGetCommentById() throws Exception {
        when(commentService.getCommentById(commentId)).thenReturn(comment);

        mockMvc.perform(get("/api/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));

        verify(commentService, times(1)).getCommentById(commentId);
    }

    @Test
    void testGetAllComments() throws Exception {
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setContent("Another comment");
        comment2.setUserId(UUID.randomUUID());
        comment2.setReadingId(UUID.randomUUID());
        comment2.setCreatedAt(new Date());
        comment2.setUpdatedAt(new Date());

        when(commentService.getAllComments()).thenReturn(Arrays.asList(comment, comment2));

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(commentService, times(1)).getAllComments();
    }

    @Test
    void testGetCommentsByReadingId() throws Exception {
        when(commentService.getCommentsByReadingId(readingId)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/reading/{readingId}", readingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()));

        verify(commentService, times(1)).getCommentsByReadingId(readingId);
    }

    @Test
    void testGetRepliesByParentId() throws Exception {
        UUID parentId = UUID.randomUUID();
        Comment reply = new Comment();
        reply.setId(UUID.randomUUID());
        reply.setParentCommentId(parentId);
        reply.setContent("A reply");
        reply.setUserId(UUID.randomUUID());
        reply.setReadingId(UUID.randomUUID());
        reply.setCreatedAt(new Date());
        reply.setUpdatedAt(new Date());

        when(commentService.getRepliesByParentId(parentId)).thenReturn(List.of(reply));

        mockMvc.perform(get("/api/comments/parent/{parentId}", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(commentService, times(1)).getRepliesByParentId(parentId);
    }

    @Test
    void testGetCommentsByUserId() throws Exception {
        when(commentService.getCommentsByUserId(userId)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));

        verify(commentService, times(1)).getCommentsByUserId(userId);
    }
}
