package com.bsuir.taskmanager.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @Schema(description = "User id", example = "1")
    private Long id;

    @Schema(description = "Username", example = "jdoe")
    private String username;

    @Schema(description = "Email", example = "jdoe@example.com")
    private String email;
}
