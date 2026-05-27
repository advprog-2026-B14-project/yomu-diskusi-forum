package id.ac.ui.cs.advprog.yomuforum.util;

import id.ac.ui.cs.advprog.yomuforum.exception.InvalidInputException;
import java.util.UUID;

public class ControllerUtils {

    private ControllerUtils() {
        // Prevent instantiation
    }

    public static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Invalid UUID format: " + value);
        }
    }

    public static UUID parseRequiredUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(fieldName + " is required");
        }
        return parseUuid(value);
    }

    public static boolean isAdmin(String userRole) {
        return userRole != null && userRole.equalsIgnoreCase("ADMIN");
    }

    public static String resolveUserId(String headerValue, String bodyValue) {
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        return bodyValue;
    }
}
