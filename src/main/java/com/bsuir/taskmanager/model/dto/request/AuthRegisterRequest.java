package com.bsuir.taskmanager.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {
    @Schema(description = "Username", example = "jdoe")
    @NotBlank
    @Size(max = 50)
    private String username;

    @Schema(description = "Email", example = "jdoe@example.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Schema(description = "Raw password", example = "MySecurePass123")
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
}
