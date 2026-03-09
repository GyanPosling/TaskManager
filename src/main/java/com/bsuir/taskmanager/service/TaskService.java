package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    List<TaskResponse> findAll();

    List<TaskResponse> findAllWithTags();

    List<TaskResponse> findAllWithComments();

    TaskResponse findById(Long id);

    List<TaskResponse> findByStatus(TaskStatus status);

    Page<TaskResponse> findByProjectOwnerAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

    Page<TaskResponse> findByTagNameAndDueDate(String tagName, LocalDate dueDate, Pageable pageable);

    TaskResponse create(TaskRequest request);

    TaskResponse createTaskWithTagAndCommentNoTx(TaskCompositeRequest request);

    TaskResponse createTaskWithTagAndCommentTx(TaskCompositeRequest request);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}
