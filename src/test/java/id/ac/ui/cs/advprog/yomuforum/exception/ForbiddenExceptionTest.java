package id.ac.ui.cs.advprog.yomuforum.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExceptionTest {

    @Test
    void testExceptionMessage() {
        ForbiddenException ex = new ForbiddenException("Forbidden action");
        assertEquals("Forbidden action", ex.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ForbiddenException ex = new ForbiddenException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
