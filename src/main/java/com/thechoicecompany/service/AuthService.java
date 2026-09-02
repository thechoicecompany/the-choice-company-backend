package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.LoginRequest;
import com.thechoicecompany.dto.response.AuthResponse;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.repository.UserRepository;
import com.thechoicecompany.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        // Spring Security validates credentials — throws BadCredentialsException on failure
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        log.info("Admin login successful: {}", user.getEmail());

        return AuthResponse.builder()
            .token(token)
            .expiresIn(86400L)
            .user(AuthResponse.UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build())
            .build();
    }
}
