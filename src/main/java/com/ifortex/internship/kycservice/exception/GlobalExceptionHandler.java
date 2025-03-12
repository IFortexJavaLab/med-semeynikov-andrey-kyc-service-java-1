package com.ifortex.internship.kycservice.exception;

import com.ifortex.internship.medstarter.exception.MedServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MESSAGE = "message";

    @ExceptionHandler(MedServiceException.class)
    public ResponseEntity<Map<String, String>> handleAuthServiceExceptions(MedServiceException ex) {

        ResponseStatus statusAnnotation = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status =
            statusAnnotation != null ? statusAnnotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put(MESSAGE, ex.getMessage());

        return ResponseEntity.status(status).body(responseBody);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        bindingResult
            .getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
        ConstraintViolationException ex) {
        log.error(ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestPartException(
        MissingServletRequestPartException ex) {
        log.error(ex.getBody().toString());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put(MESSAGE, ex.getMessage());
        return ResponseEntity.badRequest().body(responseBody);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> responseBody = new HashMap<>();
        String errorMessage;
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            errorMessage = String.format("Invalid UUID format for parameter '%s': '%s'",
                ex.getName(), ex.getValue());
            responseBody.put(MESSAGE, errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
        errorMessage = "Invalid parameter: " + ex.getMessage();
        responseBody.put(MESSAGE, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherExceptions(Exception ex) {
        log.error(ex.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An unexpected error occurred");
    }
}
