package id.ac.ui.cs.advprog.yomuforum.controller;

import id.ac.ui.cs.advprog.yomuforum.model.Reaction;
import id.ac.ui.cs.advprog.yomuforum.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Reaction> addReaction(@RequestBody Reaction reaction) {
        Reaction addedReaction = reactionService.addReaction(reaction);
        return new ResponseEntity<>(addedReaction, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeReaction(@PathVariable UUID id) {
        reactionService.removeReaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<Reaction>> getReactionsByCommentId(@PathVariable UUID commentId) {
        List<Reaction> reactions = reactionService.getReactionsByCommentId(commentId);
        return ResponseEntity.ok(reactions);
    }

    @GetMapping("/comment/{commentId}/count")
    public ResponseEntity<Long> countReactionsByType(
            @PathVariable UUID commentId,
            @RequestParam String type) {
        long count = reactionService.countReactionsByType(commentId, type);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/comment/{commentId}/user/{userId}")
    public ResponseEntity<Reaction> getUserReaction(
            @PathVariable UUID commentId,
            @PathVariable UUID userId) {
        Reaction reaction = reactionService.getUserReaction(commentId, userId);
        if (reaction != null) {
            return ResponseEntity.ok(reaction);
        }
        return ResponseEntity.notFound().build();
    }
}
