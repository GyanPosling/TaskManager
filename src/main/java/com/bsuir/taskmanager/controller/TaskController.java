package com.bsuir.taskmanager.controller;

import com.bsuir.taskmanager.controller.api.TaskControllerApi;
import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
@AllArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController implements TaskControllerApi {
    private static final int MAX_PAGE_SIZE = 10;

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

    @GetMapping("/search/by-project-owner")
    public ResponseEntity<Page<TaskResponse>> getByProjectOwnerAndStatus(
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("status") TaskStatus status,
            @PageableDefault(sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.findByProjectOwnerAndStatus(
                ownerId,
                status,
                limitPageSize(pageable)
        ));
    }

    @GetMapping("/search/by-tag/jpql")
    public ResponseEntity<Page<TaskResponse>> getByTagAndDueDateJpql(
            @RequestParam("tagName") String tagName,
            @RequestParam("dueDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDate,
            @PageableDefault(sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.findByTagNameAndDueDateJpql(
                tagName,
                dueDate,
                limitPageSize(pageable)
        ));
    }

    @GetMapping("/search/by-tag/native")
    public ResponseEntity<Page<TaskResponse>> getByTagAndDueDateNative(
            @RequestParam("tagName") String tagName,
            @RequestParam("dueDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDate,
            @PageableDefault(sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.findByTagNameAndDueDateNative(
                tagName,
                dueDate,
                limitPageSize(pageable)
        ));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody TaskRequest request) {
        TaskResponse response = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/no-tx")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentNoTx(
            @RequestBody TaskCompositeRequest request) {
        TaskResponse response = taskService.createTaskWithTagAndCommentNoTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-tag-and-comment/tx")
    public ResponseEntity<TaskResponse> createTaskWithTagAndCommentTx(
            @RequestBody TaskCompositeRequest request
    ) {
        TaskResponse response = taskService.createTaskWithTagAndCommentTx(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable("id") Long id,
                                               @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable limitPageSize(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }
}
