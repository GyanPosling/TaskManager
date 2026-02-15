package com.bsuir.taskmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    @Schema(description = "Project name", example = "Release Planning")
    @NotBlank
    @Size(max = 150)
    private String name;

    @Schema(description = "Project description", example = "Plan the Q3 release")
    @Size(max = 1000)
    private String description;

    @Schema(description = "Owner user id", example = "2")
    @NotNull
    @Positive
    private Long ownerId;
}
