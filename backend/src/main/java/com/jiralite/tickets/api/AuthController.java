package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.api.dto.LoginRequest;
import com.jiralite.tickets.api.dto.LoginResponse;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final MessageResolver messages;

    public AuthController(AuthService auth, MessageResolver messages) {
        this.auth = auth;
        this.messages = messages;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", auth.login(request)));
    }
}
