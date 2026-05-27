package id.ac.ui.cs.advprog.yomuforum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFound(CommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler(ReactionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReactionNotFound(ReactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Map.of(MESSAGE_KEY, ex.getMessage()));
    }

        @ExceptionHandler({
            InvalidInputException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class
        })
        public ResponseEntity<Map<String, String>>
        handleBadRequest(
            Exception ex
        ) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(MESSAGE_KEY, ex.getMessage()));
        }
}