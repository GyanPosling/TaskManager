package com.bsuir.taskmanager.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsyncBulkTaskStatusResponse {
    @Schema(description = "Async bulk operation id", example = "1")
    private Long operationId;

    @Schema(description = "Current operation status", example = "IN_PROGRESS")
    private AsyncBulkTaskStatus status;

    @Schema(description = "How many tasks have been processed", example = "2")
    private int processedTasks;

    @Schema(description = "How many tasks have been created", example = "2")
    private int createdTasks;

    @Schema(description = "Created task ids", example = "[10, 11]")
    private List<Long> createdTaskIds;

    @Schema(description = "Error message if the operation failed", example = "")
    private String errorMessage;
}
