package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.LoginRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.AuthResponse;
import com.thechoicecompany.security.JwtTokenProvider;
import com.thechoicecompany.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin login and token management")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider; // ── added ──

    @PostMapping("/login")
    @Operation(summary = "Admin login — returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    // ── added: lightweight endpoint used by Next.js middleware ──
    @GetMapping("/verify")
    @Operation(summary = "Verify a JWT token — used by frontend middleware")
    public ResponseEntity<Map<String, Boolean>> verify(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        boolean valid = jwtTokenProvider.validateToken(token);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false));
        }

        return ResponseEntity.ok(Map.of("valid", true));
    }
}
