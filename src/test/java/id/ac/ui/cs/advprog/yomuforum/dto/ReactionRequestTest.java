package id.ac.ui.cs.advprog.yomuforum.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReactionRequestTest {

    @Test
    void testGettersAndSetters() {
        ReactionRequest request = new ReactionRequest();
        request.setCommentId("comment-id");
        request.setUserId("user-id");
        request.setReactionType("UPVOTE");

        assertEquals("comment-id", request.getCommentId());
        assertEquals("user-id", request.getUserId());
        assertEquals("UPVOTE", request.getReactionType());
    }

    @Test
    void testDefaultValues() {
        ReactionRequest request = new ReactionRequest();
        assertNull(request.getCommentId());
        assertNull(request.getUserId());
        assertNull(request.getReactionType());
    }

    @Test
    void testEqualsAndHashCode() {
        ReactionRequest r1 = new ReactionRequest();
        r1.setCommentId("comment");
        r1.setUserId("user");
        r1.setReactionType("UPVOTE");

        ReactionRequest r2 = new ReactionRequest();
        r2.setCommentId("comment");
        r2.setUserId("user");
        r2.setReactionType("UPVOTE");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        ReactionRequest request = new ReactionRequest();
        request.setReactionType("DOWNVOTE");
        assertNotNull(request.toString());
        assertTrue(request.toString().contains("DOWNVOTE"));
    }
}
