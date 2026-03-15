package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Tasks", description = "Task CRUD operations")
public interface TaskControllerApi {
    String BULK_TASKS_REQUEST_EXAMPLE = """
            [
              {
                "title": "Design task search API",
                "description": "Prepare DTOs and endpoint contract",
                "status": "TODO",
                "dueDate": "2026-03-20",
                "projectId": 1,
                "assigneeId": 1,
                "tagIds": [1]
              },
              {
                "title": "Implement transactional bulk create",
                "description": "Add service logic for tx and no-tx modes",
                "status": "IN_PROGRESS",
                "dueDate": "2026-03-21",
                "projectId": 1,
                "assigneeId": 1,
                "tagIds": [1, 2]
              },
              {
                "title": "Write Mockito tests for services",
                "description": "Cover happy path and failure scenarios",
                "status": "DONE",
                "dueDate": "2026-03-22",
                "projectId": 1,
                "assigneeId": 2,
                "tagIds": [2]
              }
            ]
            """;

    @Operation(summary = "Get all tasks", description = "Optional filtering by status")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<TaskResponse>> getAll(
            @Parameter(description = "Task status filter") TaskStatus status
    );

    @Operation(summary = "Get all tasks with tags")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<TaskResponse>> getAllWithTags();

    @Operation(summary = "Get all tasks with comments")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<TaskResponse>> getAllWithComments();

    @Operation(summary = "Get task by id")
    @ApiResponse(responseCode = "200", description = "Task found")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TaskResponse> getById(@Positive Long id);

    @Operation(summary = "Find tasks by project owner and status using JPQL")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Page<TaskResponse>> getByProjectOwnerAndStatus(
            @Parameter(description = "Project owner id") @Positive Long ownerId,
            @Parameter(description = "Task status") TaskStatus status,
            Pageable pageable
    );

    @Operation(summary = "Find tasks by tag and due date using JPQL")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Page<TaskResponse>> getByTagAndDueDateJpql(
            @Parameter(description = "Tag name") @NotBlank String tagName,
            @Parameter(description = "Latest allowed due date") LocalDate dueDate,
            Pageable pageable
    );

    @Operation(summary = "Find tasks by tag and due date using native query")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Page<TaskResponse>> getByTagAndDueDateNative(
            @Parameter(description = "Tag name") @NotBlank String tagName,
            @Parameter(description = "Latest allowed due date") LocalDate dueDate,
            Pageable pageable
    );

    @Operation(summary = "Create task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TaskResponse> create(@Valid TaskRequest request);

    @Operation(summary = "Bulk create tasks without transaction")
    @ApiResponse(responseCode = "201", description = "Tasks created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Three bulk tasks",
                            value = BULK_TASKS_REQUEST_EXAMPLE
                    )
            )
    )
    ResponseEntity<List<TaskResponse>> createBulkNoTx(
            @Valid @Size(min = 1, max = 100) List<@Valid TaskRequest> requests,
            @Parameter(description = "Force failure after N successfully saved tasks")
            @Positive Integer failAfterIndex
    );

    @Operation(summary = "Bulk create tasks with transaction")
    @ApiResponse(responseCode = "201", description = "Tasks created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Three bulk tasks",
                            value = BULK_TASKS_REQUEST_EXAMPLE
                    )
            )
    )
    ResponseEntity<List<TaskResponse>> createBulkTx(
            @Valid @Size(min = 1, max = 100) List<@Valid TaskRequest> requests,
            @Parameter(description = "Force failure after N successfully saved tasks")
            @Positive Integer failAfterIndex
    );

    @Operation(summary = "Start async bulk task creation")
    @ApiResponse(responseCode = "202", description = "Async bulk task creation started")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Three bulk tasks",
                            value = BULK_TASKS_REQUEST_EXAMPLE
                    )
            )
    )
    ResponseEntity<AsyncBulkTaskStatusResponse> createBulkAsync(
            @Valid @Size(min = 1, max = 100) List<@Valid TaskRequest> requests
    );

    @Operation(summary = "Get async bulk task creation status")
    @ApiResponse(responseCode = "200", description = "Async bulk task status returned")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<AsyncBulkTaskStatusResponse> getBulkAsyncStatus(@Positive Long operationId);

    @Operation(summary = "Update task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TaskResponse> update(@Positive Long id, @Valid TaskRequest request);

    @Operation(summary = "Delete task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Void> delete(@Positive Long id);
}
