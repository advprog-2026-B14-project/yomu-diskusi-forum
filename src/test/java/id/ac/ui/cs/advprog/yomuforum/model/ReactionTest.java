package id.ac.ui.cs.advprog.yomuforum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReactionTest {

    private Reaction reaction;

    @BeforeEach
    void setUp() {
        reaction = new Reaction();
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        reaction.setId(id);
        assertEquals(id, reaction.getId());
    }

    @Test
    void testSetAndGetCommentId() {
        UUID commentId = UUID.randomUUID();
        reaction.setCommentId(commentId);
        assertEquals(commentId, reaction.getCommentId());
    }

    @Test
    void testSetAndGetUserId() {
        UUID userId = UUID.randomUUID();
        reaction.setUserId(userId);
        assertEquals(userId, reaction.getUserId());
    }

    @Test
    void testSetAndGetReactionType() {
        String type = "LIKE";
        reaction.setReactionType(type);
        assertEquals(type, reaction.getReactionType());
    }

    @Test
    void testCreatedAtHasDefaultValue() {
        assertNotNull(reaction.getCreatedAt());
    }

    @Test
    void testSetAndGetCreatedAt() {
        Date date = new Date();
        reaction.setCreatedAt(date);
        assertEquals(date, reaction.getCreatedAt());
    }
}
