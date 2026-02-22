package com.bsuir.taskmanager.controller;

import com.bsuir.taskmanager.controller.api.TaskControllerApi;
import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.service.TaskService;
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
public class TaskController implements TaskControllerApi {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(
            @RequestParam(name = "status", required = false) TaskStatus status
    ) {
        List<TaskResponse> response = status == null
                ? taskService.findAll()
                : taskService.findByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-tags")
    public ResponseEntity<List<TaskResponse>> getAllWithTags() {
        return ResponseEntity.ok(taskService.findAllWithTags());
    }

    @GetMapping("/with-comments")
    public ResponseEntity<List<TaskResponse>> getAllWithComments() {
        return ResponseEntity.ok(taskService.findAllWithComments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/no-tx")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentNoTx(
            @Valid @RequestBody TaskCompositeRequest request
    ) {
        TaskResponse response = taskService.createTaskWithTagAndCommentNoTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/tx")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentTx(
            @Valid @RequestBody TaskCompositeRequest request
    ) {
        TaskResponse response = taskService.createTaskWithTagAndCommentTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
