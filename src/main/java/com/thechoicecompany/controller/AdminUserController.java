package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateUserRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.UserResponse;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")   // class-level — all methods require SUPER_ADMIN
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(userRepository.save(user)), "User created successfully"));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Authenticated user not found: " + auth.getName()));
            if (currentUser.getId().equals(id) && Boolean.TRUE.equals(currentUser.getIsActive())) {
                throw new IllegalStateException("You cannot deactivate your own account.");
            }
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(!user.getIsActive());
        return ResponseEntity.ok(ApiResponse.success(toResponse(userRepository.save(user)), "User status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    // ── Mapper — password never touches this method ───────────────────────────
    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole())
                .isActive(u.getIsActive())
                .lastLogin(u.getLastLogin())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}