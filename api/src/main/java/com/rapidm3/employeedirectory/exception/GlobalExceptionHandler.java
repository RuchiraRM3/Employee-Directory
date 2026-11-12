package com.rapidm3.employeedirectory.exception;

import com.rapidm3.employeedirectory.dto.ApiError;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Maps exceptions to the ApiError JSON shape. Messages are written for HR users;
 * stack traces, SQL and exception internals are never returned.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Some fields are invalid.", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex) {
        return respond(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is malformed or contains an invalid value (check dates and status).", Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex) {
        return respond(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "Parameter '" + ex.getName() + "' has an invalid value.", Map.of());
    }

    @ExceptionHandler(InvalidDepartmentException.class)
    ResponseEntity<ApiError> invalidDepartment(InvalidDepartmentException ex) {
        return respond(HttpStatus.BAD_REQUEST, "INVALID_DEPARTMENT", "Some fields are invalid.",
                Map.of("departmentId", ex.getMessage()));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    ResponseEntity<ApiError> notFound(EmployeeNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiError> duplicateEmail(DuplicateEmailException ex) {
        if (ex.isArchived()) {
            return respond(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "An employee with this email already exists.",
                    Map.of("email", "This email belongs to an archived (deleted) employee."));
        }
        return duplicateEmailResponse();
    }

    /** Backstop for races: two requests pass the service pre-check, the DB unique key rejects one. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detail.contains("uk_employee_email")) {
            return duplicateEmailResponse();
        }
        log.warn("Unexpected data integrity violation", ex);
        return respond(HttpStatus.CONFLICT, "DATA_CONFLICT", "The change conflicts with existing data.", Map.of());
    }

    /** Framework-level HTTP errors (wrong method, content type, unknown path) in the same ApiError shape. */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class,
            NoResourceFoundException.class})
    ResponseEntity<ApiError> httpError(Exception ex) {
        HttpStatus status = HttpStatus.valueOf(((ErrorResponse) ex).getStatusCode().value());
        String message = switch (status) {
            case METHOD_NOT_ALLOWED -> "This request method is not supported for this address.";
            case UNSUPPORTED_MEDIA_TYPE -> "Send the request body as JSON (Content-Type: application/json).";
            default -> "No such address.";
        };
        return respond(status, status.name(), message, Map.of());
    }

    /** Last resort: log the details server-side, return a plain message (never a stack trace). */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unhandled error", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Something went wrong on the server. Please try again.", Map.of());
    }

    private ResponseEntity<ApiError> duplicateEmailResponse() {
        return respond(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "An employee with this email already exists.",
                Map.of("email", "Email is already in use."));
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String code, String message,
                                             Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, fieldErrors));
    }
}
