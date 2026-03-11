package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Tasks", description = "Task CRUD operations")
public interface TaskControllerApi {
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

    @Operation(summary = "Create task, tag, comment without transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TaskResponse> createTaskWithTagAndCommentNoTx(
            @Valid TaskCompositeRequest request
    );

    @Operation(summary = "Create task, tag, comment with transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TaskResponse> createTaskWithTagAndCommentTx(
            @Valid TaskCompositeRequest request
    );

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
