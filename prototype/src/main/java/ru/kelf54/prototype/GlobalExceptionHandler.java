package ru.kelf54.prototype;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kelf54.starter.exception.QueueFullException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {
        return new ApiError("Validation error", ex.getMessage());
    }

    @ExceptionHandler(QueueFullException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiError handleQueueFull(QueueFullException ex) {
        return new ApiError("Queue overflow", ex.getMessage());
    }
}

record ApiError(String code, String message) {}
