package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long expiresIn;
    private UserSummary user;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String email;
        private String fullName;
        private String role;
    }
}
