package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {
	@NotBlank
	@Email
	private String email;
	@NotBlank
	@Size(min = 8)
	private String password;
	@NotBlank
	private String fullName;
	@NotNull
	private UserRole role;
}
