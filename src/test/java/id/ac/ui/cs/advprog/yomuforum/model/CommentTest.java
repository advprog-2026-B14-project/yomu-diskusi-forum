package id.ac.ui.cs.advprog.yomuforum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        comment.setId(id);
        assertEquals(id, comment.getId());
    }

    @Test
    void testSetAndGetUserId() {
        UUID userId = UUID.randomUUID();
        comment.setUserId(userId);
        assertEquals(userId, comment.getUserId());
    }

    @Test
    void testSetAndGetReadingId() {
        UUID readingId = UUID.randomUUID();
        comment.setReadingId(readingId);
        assertEquals(readingId, comment.getReadingId());
    }

    @Test
    void testSetAndGetParentCommentId() {
        UUID parentId = UUID.randomUUID();
        comment.setParentCommentId(parentId);
        assertEquals(parentId, comment.getParentCommentId());
    }

    @Test
    void testParentCommentIdCanBeNull() {
        comment.setParentCommentId(null);
        assertNull(comment.getParentCommentId());
    }

    @Test
    void testSetAndGetContent() {
        String content = "This is a test comment";
        comment.setContent(content);
        assertEquals(content, comment.getContent());
    }

    @Test
    void testCreatedAtHasDefaultValue() {
        assertNotNull(comment.getCreatedAt());
    }

    @Test
    void testSetAndGetCreatedAt() {
        Date date = new Date();
        comment.setCreatedAt(date);
        assertEquals(date, comment.getCreatedAt());
    }

    @Test
    void testUpdatedAtHasDefaultValue() {
        assertNotNull(comment.getUpdatedAt());
    }

    @Test
    void testSetAndGetUpdatedAt() {
        Date date = new Date();
        comment.setUpdatedAt(date);
        assertEquals(date, comment.getUpdatedAt());
    }
}
