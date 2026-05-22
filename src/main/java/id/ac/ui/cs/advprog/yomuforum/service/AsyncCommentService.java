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

/**
 * Service yang menyediakan operasi ASYNCHRONOUS untuk Comment dan Reaction.
 *
 * Menggunakan @Async agar setiap method berjalan di thread terpisah
 * dari thread pool "taskExecutor" (lihat AsyncConfig).
 *
 * Kegunaan utama:
 * 1. Parallel data fetching — ambil comments dan reactions secara bersamaan
 * 2. Non-blocking tree building — build comment tree di background thread
 * 3. Bulk operations — proses batch tanpa memblokir request utama
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncCommentService {

    private final CommentService commentService;
    private final ReactionService reactionService;

    /**
     * Mengambil semua komentar berdasarkan readingId secara ASYNC.
     * Return CompletableFuture agar bisa di-compose dengan operasi lain.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<Comment>> getCommentsByReadingIdAsync(UUID readingId) {
        log.debug("Async fetching comments for reading {} on thread {}",
                readingId, Thread.currentThread().getName());
        List<Comment> comments = commentService.getCommentsByReadingId(readingId);
        return CompletableFuture.completedFuture(comments);
    }

    /**
     * Membangun comment tree secara ASYNC di background thread.
     * Operasi tree-building bisa CPU-intensive untuk thread dengan banyak komentar.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<CommentComponent>> getCommentTreeAsync(UUID readingId) {
        log.debug("Async building comment tree for reading {} on thread {}",
                readingId, Thread.currentThread().getName());
        List<CommentComponent> tree = commentService.getCommentTreeByReadingId(readingId);
        return CompletableFuture.completedFuture(tree);
    }

    /**
     * Mengambil reactions untuk sebuah comment secara ASYNC.
     */
    @Async("taskExecutor")
    public CompletableFuture<List<Reaction>> getReactionsByCommentIdAsync(UUID commentId) {
        log.debug("Async fetching reactions for comment {} on thread {}",
                commentId, Thread.currentThread().getName());
        List<Reaction> reactions = reactionService.getReactionsByCommentId(commentId);
        return CompletableFuture.completedFuture(reactions);
    }

    /**
     * PARALLEL FETCH: Mengambil comments dan reactions secara bersamaan.
     *
     * Tanpa async: total = waktu_comments + waktu_reactions (sequential)
     * Dengan async: total = max(waktu_comments, waktu_reactions) (parallel)
     *
     * Contoh improvement: 200ms + 150ms = 350ms → max(200ms, 150ms) = 200ms (~43% faster)
     */
    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> getCommentsWithReactionsAsync(
            UUID readingId, UUID commentId) {
        log.info("Parallel fetch: comments for reading {} + reactions for comment {} on thread {}",
                readingId, commentId, Thread.currentThread().getName());

        CompletableFuture<List<Comment>> commentsFuture =
                getCommentsByReadingIdAsync(readingId);
        CompletableFuture<List<Reaction>> reactionsFuture =
                getReactionsByCommentIdAsync(commentId);

        // Tunggu keduanya selesai secara paralel
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
