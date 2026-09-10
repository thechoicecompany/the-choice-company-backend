package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateUserRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.UserResponse;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.enums.UserRole;
import com.thechoicecompany.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/dev/bootstrap")
@RequiredArgsConstructor
@Slf4j
// @Profile("dev") is still here — the bean won't exist in prod at all.
// The runtime guard below is a second line of defence for misconfigured profiles.
@Profile("dev")
public class BootstrapController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<UserResponse>> createFirstAdmin(
            @Valid @RequestBody CreateUserRequest request) {

        // Paranoia check: refuse to run if the active profiles include prod or staging.
        // This protects against environment variable typos like SPRING_PROFILES_ACTIVE=dev,prod
        String[] activeProfiles = environment.getActiveProfiles();
        boolean hasNonDevProfile = Arrays.stream(activeProfiles)
                .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("staging"));

        if (hasNonDevProfile) {
            log.error("Bootstrap endpoint called with non-dev profiles active: {}. Refusing.",
                    Arrays.toString(activeProfiles));
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bootstrap is disabled in this environment."));
        }

        if (userRepository.count() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Bootstrap disabled: users already exist"));
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.SUPER_ADMIN)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Bootstrap: first SUPER_ADMIN created — {}", saved.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(saved), "First SUPER_ADMIN created successfully"));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}