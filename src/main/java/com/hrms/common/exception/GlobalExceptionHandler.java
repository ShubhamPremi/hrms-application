package com.hrms.common.exception;

import com.hrms.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// It intercepts exceptions thrown from ANY controller in the application
// Without this, Spring returns a default HTML error page — useless for REST APIs
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles: employee not found by id or email
    // HTTP 404 — the resource the client asked for does not exist
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(
            EmployeeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), List.of(ex.getMessage())));
    }

    // Handles: duplicate email on create/update
    // HTTP 409 Conflict — the request conflicts with existing data
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), List.of(ex.getMessage())));
    }

    // Handles: @Valid failures on request DTOs — blank name, invalid email etc.
    // HTTP 400 Bad Request — the client sent malformed data
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Extract each field error into a readable string: "name: Name is required"
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    // Handles: /api/v1/employees/abc when id must be a Long
    // HTTP 400 Bad Request
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String error = "Parameter '" + ex.getName() + "' must be of type " +
                ex.getRequiredType().getSimpleName();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid parameter type", List.of(error)));
    }

    // Handles: anything not caught above — last line of defence
    // HTTP 500 — we do not know what went wrong, log it and return generic message
    // IMPORTANT: never expose the raw exception message to the client in production
    // It can leak internal details (SQL, file paths, class names) to attackers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later.",
                        List.of("Contact support if this persists")
                ));
    }
}