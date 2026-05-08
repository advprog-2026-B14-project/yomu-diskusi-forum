package id.ac.ui.cs.advprog.yomuforum.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidInputExceptionTest {

    @Test
    void testExceptionMessage() {
        InvalidInputException ex = new InvalidInputException("Bad input");
        assertEquals("Bad input", ex.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidInputException ex = new InvalidInputException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
