package com.inkwell.web.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.inkwell.web")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                message,
                safePath(exception)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiErrorResponse> handleDownstreamFailure(RestClientException exception) {
        log.error("Gateway downstream service call failed", exception);
        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Downstream Service Unavailable",
                "A required InkWell microservice is temporarily unavailable.",
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled gateway exception", exception);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred while processing the request.",
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private String safePath(MethodArgumentNotValidException exception) {
        if (exception.getParameter() != null && exception.getParameter().getMethod() != null) {
            return exception.getParameter().getMethod().getName();
        }

        return null;
    }

    @Getter
    @Builder
    private static class ApiErrorResponse {
        private final LocalDateTime timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;
    }
}
