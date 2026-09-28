package com.jiralite.tickets.error;

import com.jiralite.tickets.api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageResolver messages;

    public GlobalExceptionHandler(MessageResolver messages) {
        this.messages = messages;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex) {
        return envelope(ex.getHttpStatus(), ex.getCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        return envelope(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return envelope(HttpStatus.UNAUTHORIZED, ErrorCodes.AUTHENTICATION_FAILED);
    }

    private ResponseEntity<ApiResponse<Void>> envelope(HttpStatus status, String code) {
        return ResponseEntity.status(status).body(ApiResponse.failed(messages.message(code), code));
    }
}
