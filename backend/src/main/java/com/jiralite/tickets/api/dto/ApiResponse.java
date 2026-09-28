package com.jiralite.tickets.api.dto;

public record ApiResponse<T>(String message, String code, String status, T data) {
    public static <T> ApiResponse<T> success(String message, String code, T data) {
        return new ApiResponse<>(message, code, "success", data);
    }

    public static <T> ApiResponse<T> failed(String message, String code) {
        return new ApiResponse<>(message, code, "failed", null);
    }
}
