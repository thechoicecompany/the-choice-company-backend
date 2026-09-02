package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateUserRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.enums.UserRole;
import com.thechoicecompany.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/bootstrap")
@RequiredArgsConstructor
@Profile("dev")
public class BootstrapController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<User>> createFirstAdmin(
            @Valid @RequestBody CreateUserRequest request) {

        if (userRepository.count() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "Bootstrap disabled: users already exist"));
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.SUPER_ADMIN)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        saved.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        saved,
                        "First SUPER_ADMIN created successfully"));
    }
}