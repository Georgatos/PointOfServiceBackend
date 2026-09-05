package dev.andreasgeorgatos.pointofservicebackend.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException e) {
        return toResponse(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return toResponse(HttpStatus.CONFLICT, e);
    }

    private ResponseEntity<Map<String, String>> toResponse(HttpStatus status, Exception e) {
        String message = e.getMessage() == null ? status.getReasonPhrase() : e.getMessage();

        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
