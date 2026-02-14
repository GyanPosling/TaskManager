package com.bsuir.taskmanager.dto.request;

import com.bsuir.taskmanager.model.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @Schema(description = "Task title", example = "Fix build pipeline")
    @NotBlank
    @Size(max = 200)
    private String title;

    @Schema(description = "Task description", example = "Update CI and fix failing tests")
    @Size(max = 2000)
    private String description;

    @Schema(description = "Task status", example = "IN_PROGRESS")
    @NotNull
    private TaskStatus status;

    @Schema(description = "Due date", example = "2026-02-20")
    @FutureOrPresent
    private LocalDate dueDate;

    @Schema(description = "Project id", example = "1")
    @NotNull
    @Positive
    private Long projectId;

    @Schema(description = "Assignee user id", example = "3")
    @Positive
    private Long assigneeId;

    @Schema(description = "Tag ids", example = "[2, 5, 7]")
    @Size(max = 50)
    private Set<@Positive Long> tagIds;
}
