package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(UUID.randomUUID());
        comment.setReadingId(UUID.randomUUID());
        comment.setContent("Test comment content");
    }

    @Test
    void testCreateComment() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(comment);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testCreateCommentSetsNewId() {
        UUID originalId = comment.getId();
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.createComment(comment);

        assertNotNull(result.getId());
        assertNotEquals(originalId, result.getId());
    }

    @Test
    void testUpdateCommentSuccess() {
        Comment updatedComment = new Comment();
        updatedComment.setContent("Updated content");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.updateComment(commentId, updatedComment);

        assertEquals("Updated content", result.getContent());
        assertNotNull(result.getUpdatedAt());
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testUpdateCommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        Comment updatedComment = new Comment();
        updatedComment.setContent("Updated content");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> commentService.updateComment(commentId, updatedComment));

        assertEquals("Comment not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testDeleteComment() {
        doNothing().when(commentRepository).deleteById(commentId);

        commentService.deleteComment(commentId);

        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void testGetCommentByIdSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(commentId);

        assertEquals(commentId, result.getId());
        assertEquals("Test comment content", result.getContent());
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testGetCommentByIdNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> commentService.getCommentById(commentId));

        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    void testGetAllComments() {
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setContent("Another comment");

        when(commentRepository.findAll()).thenReturn(Arrays.asList(comment, comment2));

        List<Comment> results = commentService.getAllComments();

        assertEquals(2, results.size());
        verify(commentRepository, times(1)).findAll();
    }

    @Test
    void testGetAllCommentsEmpty() {
        when(commentRepository.findAll()).thenReturn(List.of());

        List<Comment> results = commentService.getAllComments();

        assertTrue(results.isEmpty());
    }

    @Test
    void testGetCommentsByReadingId() {
        UUID readingId = comment.getReadingId();
        when(commentRepository.findByReadingId(readingId)).thenReturn(List.of(comment));

        List<Comment> results = commentService.getCommentsByReadingId(readingId);

        assertEquals(1, results.size());
        assertEquals(comment.getId(), results.get(0).getId());
        verify(commentRepository, times(1)).findByReadingId(readingId);
    }

    @Test
    void testGetRepliesByParentId() {
        UUID parentId = UUID.randomUUID();
        Comment reply = new Comment();
        reply.setId(UUID.randomUUID());
        reply.setParentCommentId(parentId);
        reply.setContent("This is a reply");

        when(commentRepository.findByParentCommentId(parentId)).thenReturn(List.of(reply));

        List<Comment> results = commentService.getRepliesByParentId(parentId);

        assertEquals(1, results.size());
        assertEquals(parentId, results.get(0).getParentCommentId());
        verify(commentRepository, times(1)).findByParentCommentId(parentId);
    }

    @Test
    void testGetCommentsByUserId() {
        UUID userId = comment.getUserId();
        when(commentRepository.findByUserId(userId)).thenReturn(List.of(comment));

        List<Comment> results = commentService.getCommentsByUserId(userId);

        assertEquals(1, results.size());
        verify(commentRepository, times(1)).findByUserId(userId);
    }
}
