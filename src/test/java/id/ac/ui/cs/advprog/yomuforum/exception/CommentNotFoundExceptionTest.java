package id.ac.ui.cs.advprog.yomuforum.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        CommentNotFoundException ex = new CommentNotFoundException("Comment not found");
        assertEquals("Comment not found", ex.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        CommentNotFoundException ex = new CommentNotFoundException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
