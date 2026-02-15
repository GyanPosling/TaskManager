package com.bsuir.taskmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    @Schema(description = "Project id", example = "1")
    private Long id;

    @Schema(description = "Project name", example = "Release Planning")
    private String name;

    @Schema(description = "Project description", example = "Plan the Q3 release")
    private String description;

    @Schema(description = "Owner user id", example = "2")
    private Long ownerId;
}
