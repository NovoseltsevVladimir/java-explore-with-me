package ru.practicum.stats.client.exception;

import lombok.Getter;

@Getter
class ErrorResponse {
    private final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

}