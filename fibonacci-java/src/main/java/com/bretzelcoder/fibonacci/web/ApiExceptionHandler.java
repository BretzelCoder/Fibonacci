package com.bretzelcoder.fibonacci.web;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns failures on the JSON surface into RFC 9457 {@code ProblemDetail} bodies.
 *
 * <p>Every response carries a {@code message} property in addition to the standard
 * {@code detail}, because the Vue frontend shared with the .NET stack reads
 * {@code payload.message} — see {@code frontend/src/services/fibonacciApi.ts}.
 *
 * <p>Scoped to {@link FibonacciApiController} so the Thymeleaf controller keeps rendering
 * HTML error pages rather than JSON.
 */
@RestControllerAdvice(assignableTypes = FibonacciApiController.class)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() == null
                        ? error.getField() + " is invalid."
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));

        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
                message.isBlank() ? "Request validation failed." : message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unhandled exception on the Fibonacci API", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error",
                "An unexpected error occurred.");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setTitle(title);
        problem.setProperty("message", message);
        return problem;
    }
}
