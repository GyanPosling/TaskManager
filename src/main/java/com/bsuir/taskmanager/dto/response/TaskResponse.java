package com.bsuir.taskmanager.dto.response;

import com.bsuir.taskmanager.model.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class TaskResponse {
    @Schema(description = "Task id", example = "10")
    private Long id;

    @Schema(description = "Task title", example = "Fix build pipeline")
    private String title;

    @Schema(description = "Task description", example = "Update CI and fix failing tests")
    private String description;

    @Schema(description = "Task status", example = "IN_PROGRESS")
    private TaskStatus status;

    @Schema(description = "Due date", example = "2026-02-20")
    private LocalDate dueDate;

    @Schema(description = "Project id", example = "1")
    private Long projectId;

    @Schema(description = "Assignee user id", example = "3")
    private Long assigneeId;

    @Schema(description = "Tag ids", example = "[2, 5, 7]")
    private Set<Long> tagIds;
}
