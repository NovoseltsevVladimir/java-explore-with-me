package ru.practicum.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.client.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ru.practicum.client.exception.ErrorResponse handleValidationException(final ValidationException e) {
        return new ru.practicum.client.exception.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ru.practicum.client.exception.ErrorResponse handleThrowableException(final Throwable e) {
        return new ru.practicum.client.exception.ErrorResponse(e.getMessage());
    }


}
