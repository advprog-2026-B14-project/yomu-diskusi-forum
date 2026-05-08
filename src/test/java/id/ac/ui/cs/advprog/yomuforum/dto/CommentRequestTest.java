package id.ac.ui.cs.advprog.yomuforum.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentRequestTest {

    @Test
    void testGettersAndSetters() {
        CommentRequest request = new CommentRequest();
        request.setContent("Test content");
        request.setUserId("user-id");
        request.setReadingId("reading-id");
        request.setParentCommentId("parent-id");

        assertEquals("Test content", request.getContent());
        assertEquals("user-id", request.getUserId());
        assertEquals("reading-id", request.getReadingId());
        assertEquals("parent-id", request.getParentCommentId());
    }

    @Test
    void testDefaultValues() {
        CommentRequest request = new CommentRequest();
        assertNull(request.getContent());
        assertNull(request.getUserId());
        assertNull(request.getReadingId());
        assertNull(request.getParentCommentId());
    }

    @Test
    void testEqualsAndHashCode() {
        CommentRequest r1 = new CommentRequest();
        r1.setContent("content");
        r1.setUserId("user");
        r1.setReadingId("reading");

        CommentRequest r2 = new CommentRequest();
        r2.setContent("content");
        r2.setUserId("user");
        r2.setReadingId("reading");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        CommentRequest request = new CommentRequest();
        request.setContent("test");
        assertNotNull(request.toString());
        assertTrue(request.toString().contains("test"));
    }
}
