package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateUserRequest;
import com.thechoicecompany.dto.response.ApiResponse;
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
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── List all users (SUPER_ADMIN only) ────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // never expose hashes
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched"));
    }

    // ── Create user (SUPER_ADMIN only) ───────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
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
        savedUser.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully"));
    }


    // ── Toggle active/inactive (SUPER_ADMIN only) ────────────────────────────
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<User>> toggleActive(@PathVariable Long id) {
 
        // Guard: don't let an admin deactivate their own account —
        // this is what silently kills the current session and produces
        // the 403 cascade on every request right after.
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
        userRepository.save(user);
        user.setPassword(null);
 
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated"));
    }
    // ── Delete user (SUPER_ADMIN only) ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }
}