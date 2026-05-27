package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncCommentService {

    private final CommentService commentService;
    private final ReactionService reactionService;

    private final org.springframework.beans.factory.ObjectProvider<AsyncCommentService> selfProvider;

    @Async("taskExecutor")
    public CompletableFuture<List<Comment>> getCommentsByReadingIdAsync(UUID readingId) {
        log.debug("Async fetching comments for reading {} on thread {}",
                readingId, Thread.currentThread().getName());
        List<Comment> comments = commentService.getCommentsByReadingId(readingId);
        return CompletableFuture.completedFuture(comments);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<CommentComponent>> getCommentTreeAsync(UUID readingId) {
        log.debug("Async building comment tree for reading {} on thread {}",
                readingId, Thread.currentThread().getName());
        List<CommentComponent> tree = commentService.getCommentTreeByReadingId(readingId);
        return CompletableFuture.completedFuture(tree);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<Reaction>> getReactionsByCommentIdAsync(UUID commentId) {
        log.debug("Async fetching reactions for comment {} on thread {}",
                commentId, Thread.currentThread().getName());
        List<Reaction> reactions = reactionService.getReactionsByCommentId(commentId);
        return CompletableFuture.completedFuture(reactions);
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> getCommentsWithReactionsAsync(
            UUID readingId, UUID commentId) {
        log.info("Parallel fetch: comments for reading {} + reactions for comment {} on thread {}",
                readingId, commentId, Thread.currentThread().getName());

        CompletableFuture<List<Comment>> commentsFuture =
                selfProvider.getObject().getCommentsByReadingIdAsync(readingId);
        CompletableFuture<List<Reaction>> reactionsFuture =
                selfProvider.getObject().getReactionsByCommentIdAsync(commentId);

        return commentsFuture.thenCombine(reactionsFuture, (comments, reactions) -> {
            log.info("Parallel fetch complete: {} comments, {} reactions",
                    comments.size(), reactions.size());
            return Map.of(
                    "comments", comments,
                    "reactions", reactions
            );
        });
    }
}
