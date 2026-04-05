package com.bsuir.taskmanager.exception;

import com.bsuir.taskmanager.model.dto.response.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler({
            AsyncBulkTaskOperationNotFoundException.class,
            CommentNotFoundException.class,
            ProjectNotFoundException.class,
            TagNotFoundException.class,
            TagsNotFoundException.class,
            TaskNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex,
                                                        HttpServletRequest request) {
        log.debug("Resource not found for path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BulkTaskCreationException.class)
    public ResponseEntity<ErrorResponse> handleBulkTaskCreation(
            BulkTaskCreationException ex,
            HttpServletRequest request
    ) {
        log.error("Business error for path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        log.debug(
                "Authentication failed for path {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request
    ) {
        log.debug("JWT error for path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put("email", ex.getMessage());
        log.debug("Email conflict for path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(),
                request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put("username", ex.getMessage());
        log.debug("Username conflict for path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(),
                request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        log.debug("Validation failed for path {}: {}", request.getRequestURI(), fieldErrors);
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed",
                request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(violation -> fieldErrors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()));
        log.debug("Constraint violation for path {}: {}", request.getRequestURI(), fieldErrors);
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed",
                request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "Invalid value for parameter '%s'".formatted(ex.getName());
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(ex.getName(), message);
        log.debug("Parameter type mismatch for path {}: {}", request.getRequestURI(), message);
        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String message = "Required parameter '%s' is missing".formatted(ex.getParameterName());
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(ex.getParameterName(), message);
        log.debug("Missing request parameter for path {}: {}", request.getRequestURI(), message);
        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.debug("Malformed request body for path {}: {}",
                request.getRequestURI(),
                ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "Malformed request body",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String message = "HTTP method '%s' is not supported for this endpoint"
                .formatted(ex.getMethod());
        log.debug("Method not supported for path {}: {}", request.getRequestURI(), message);
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
                                                          HttpServletRequest request) {
        log.error("Unexpected error for path {}", request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error",
                request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status,
                                                     String message,
                                                     String path,
                                                     Map<String, String> fieldErrors) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors == null ? new LinkedHashMap<>() : fieldErrors)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
