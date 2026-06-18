package com.moving.reservation.api;

public record ApiErrorResponse(String code, String message) {

    public static ApiErrorResponse badRequest(String message) {
        return new ApiErrorResponse("BAD_REQUEST", message);
    }

    public static ApiErrorResponse notFound(String message) {
        return new ApiErrorResponse("NOT_FOUND", message);
    }
}
