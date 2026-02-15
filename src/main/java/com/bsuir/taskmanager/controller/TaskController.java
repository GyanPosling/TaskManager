package com.bsuir.taskmanager.controller;

import com.bsuir.taskmanager.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.dto.request.TaskRequest;
import com.bsuir.taskmanager.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task CRUD operations")
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Optional filtering by status")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public ResponseEntity<List<TaskResponse>> getAll(
            @Parameter(description = "Task status filter")
            @RequestParam(name = "status", required = false) TaskStatus status
    ) {
        List<TaskResponse> response = status == null
                ? taskService.findAll()
                : taskService.findByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-tags")
    @Operation(summary = "Get all tasks with tags")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public ResponseEntity<List<TaskResponse>> getAllWithTags() {
        return ResponseEntity.ok(taskService.findAllWithTags());
    }

    @GetMapping("/with-comments")
    @Operation(summary = "Get all tasks with comments")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public ResponseEntity<List<TaskResponse>> getAllWithComments() {
        return ResponseEntity.ok(taskService.findAllWithComments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/no-tx")
    @Operation(summary = "Create task, tag, comment without transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentNoTx(
            @Valid @RequestBody TaskCompositeRequest request
    ) {
        TaskResponse response = taskService.createTaskWithTagAndCommentNoTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/tx")
    @Operation(summary = "Create task, tag, comment with transaction")
    @ApiResponse(responseCode = "201", description = "Composite data created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentTx(
            @Valid @RequestBody TaskCompositeRequest request
    ) {
        TaskResponse response = taskService.createTaskWithTagAndCommentTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
