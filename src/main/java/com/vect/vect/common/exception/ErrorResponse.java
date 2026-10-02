package com.vect.vect.common.exception;

import java.util.List;

public record ErrorResponse(String code, String message, List<String> detalles) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse of(String code, String message, List<String> detalles) {
        return new ErrorResponse(code, message, detalles);
    }
}