package com.jiralite.tickets.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralite.tickets.api.JwtAuthFilter;
import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.error.ErrorCodes;
import com.jiralite.tickets.error.MessageResolver;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            ObjectMapper mapper,
            MessageResolver messages)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/auth/login", "/actuator/health")
                                        .permitAll()
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .exceptionHandling(
                        ex ->
                                ex.authenticationEntryPoint(
                                        (request, response, e) -> {
                                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                            response.setContentType("application/json");
                                            mapper.writeValue(
                                                    response.getOutputStream(),
                                                    ApiResponse.failed(
                                                            messages.message(ErrorCodes.AUTHENTICATION_FAILED),
                                                            ErrorCodes.AUTHENTICATION_FAILED));
                                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
