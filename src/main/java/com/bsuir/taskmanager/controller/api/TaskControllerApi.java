package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Tasks", description = "Task CRUD operations")
public interface TaskControllerApi {
    @Operation(summary = "Get all tasks", description = "Optional filtering by status")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    ResponseEntity<List<TaskResponse>> getAll(
            @Parameter(description = "Task status filter") TaskStatus status
    );

    @Operation(summary = "Get all tasks with tags")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    ResponseEntity<List<TaskResponse>> getAllWithTags();

    @Operation(summary = "Get all tasks with comments")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    ResponseEntity<List<TaskResponse>> getAllWithComments();

    @Operation(summary = "Get task by id")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    ResponseEntity<TaskResponse> getById(Long id);

    @Operation(summary = "Create task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<TaskResponse> create(TaskRequest request);

    @Operation(summary = "Create task, tag, comment without transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<TaskResponse> createTaskWithTagAndCommentNoTx(TaskCompositeRequest request);

    @Operation(summary = "Create task, tag, comment with transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<TaskResponse> createTaskWithTagAndCommentTx(TaskCompositeRequest request);

    @Operation(summary = "Update task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Task not found")
    ResponseEntity<TaskResponse> update(Long id, TaskRequest request);

    @Operation(summary = "Delete task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "404", description = "Task not found")
    ResponseEntity<Void> delete(Long id);
}
