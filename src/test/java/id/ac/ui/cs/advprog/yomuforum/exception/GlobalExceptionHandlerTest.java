package id.ac.ui.cs.advprog.yomuforum.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCommentNotFoundReturns404() {
        var response = handler.handleCommentNotFound(new CommentNotFoundException("Comment not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Comment not found", response.getBody().get("message"));
    }

    @Test
    void handleReactionNotFoundReturns404() {
        var response = handler.handleReactionNotFound(new ReactionNotFoundException("Reaction not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Reaction not found", response.getBody().get("message"));
    }

    @Test
    void handleForbiddenReturns403() {
        var response = handler.handleForbidden(new ForbiddenException("Forbidden"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().get("message"));
    }

    @Test
    void handleBadRequestReturns400() {
        var response = handler.handleBadRequest(new InvalidInputException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("message"));
    }
}
