package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateUserRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("User with this email already exists"));
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Don't return password to API clients
        savedUser.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully"));
    }
}