package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentLeaf;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncCommentServiceTest {

    @Mock
    private CommentService commentService;

    @Mock
    private ReactionService reactionService;

    @Mock
    private org.springframework.beans.factory.ObjectProvider<AsyncCommentService> selfProvider;

    @InjectMocks
    private AsyncCommentService asyncCommentService;

    private UUID readingId;
    private UUID commentId;
    private Comment comment;
    private Reaction reaction;

    @BeforeEach
    void setUp() {
        readingId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        comment = new Comment();
        comment.setId(commentId);
        comment.setReadingId(readingId);

        reaction = new Reaction();
        reaction.setId(UUID.randomUUID());
        reaction.setCommentId(commentId);
    }

    @Test
    void testGetCommentsByReadingIdAsync() throws ExecutionException, InterruptedException {
        when(commentService.getCommentsByReadingId(readingId)).thenReturn(List.of(comment));

        CompletableFuture<List<Comment>> future = asyncCommentService.getCommentsByReadingIdAsync(readingId);
        List<Comment> result = future.get();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(commentId, result.get(0).getId());
        verify(commentService, times(1)).getCommentsByReadingId(readingId);
    }

    @Test
    void testGetCommentTreeAsync() throws ExecutionException, InterruptedException {
        CommentLeaf leaf = new CommentLeaf(comment);
        when(commentService.getCommentTreeByReadingId(readingId)).thenReturn(List.of(leaf));

        CompletableFuture<List<CommentComponent>> future = asyncCommentService.getCommentTreeAsync(readingId);
        List<CommentComponent> result = future.get();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(commentId, result.get(0).getId());
        verify(commentService, times(1)).getCommentTreeByReadingId(readingId);
    }

    @Test
    void testGetReactionsByCommentIdAsync() throws ExecutionException, InterruptedException {
        when(reactionService.getReactionsByCommentId(commentId)).thenReturn(List.of(reaction));

        CompletableFuture<List<Reaction>> future = asyncCommentService.getReactionsByCommentIdAsync(commentId);
        List<Reaction> result = future.get();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reaction.getId(), result.get(0).getId());
        verify(reactionService, times(1)).getReactionsByCommentId(commentId);
    }

    @Test
    void testGetCommentsWithReactionsAsync() throws ExecutionException, InterruptedException {
        when(selfProvider.getObject()).thenReturn(asyncCommentService);
        when(commentService.getCommentsByReadingId(readingId)).thenReturn(List.of(comment));
        when(reactionService.getReactionsByCommentId(commentId)).thenReturn(List.of(reaction));

        CompletableFuture<Map<String, Object>> future = asyncCommentService.getCommentsWithReactionsAsync(readingId, commentId);
        Map<String, Object> result = future.get();

        assertNotNull(result);
        assertTrue(result.containsKey("comments"));
        assertTrue(result.containsKey("reactions"));

        List<Comment> comments = (List<Comment>) result.get("comments");
        List<Reaction> reactions = (List<Reaction>) result.get("reactions");

        assertEquals(1, comments.size());
        assertEquals(1, reactions.size());
    }
}
