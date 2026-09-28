package com.jiralite.tickets.service;

import com.jiralite.tickets.api.dto.LoginRequest;
import com.jiralite.tickets.api.dto.LoginResponse;
import com.jiralite.tickets.api.dto.UserResponse;
import com.jiralite.tickets.domain.User;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import com.jiralite.tickets.persistence.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest request) {
        User user =
                users.findByUsername(request.username())
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                ErrorCodes.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED));
        if (!passwords.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCodes.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED);
        }
        return new LoginResponse(
                jwt.issue(user.getId(), user.getUsername()),
                new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName()));
    }
}
