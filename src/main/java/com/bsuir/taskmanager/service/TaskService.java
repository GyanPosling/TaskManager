package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.dto.request.TaskRequest;
import com.bsuir.taskmanager.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.util.List;

public interface TaskService {
    List<TaskResponse> findAll();

    List<TaskResponse> findAllWithTags();

    List<TaskResponse> findAllWithComments();

    TaskResponse findById(Long id);

    List<TaskResponse> findByStatus(TaskStatus status);

    TaskResponse create(TaskRequest request);

    TaskResponse createTaskWithTagAndCommentNoTx(TaskCompositeRequest request);

    TaskResponse createTaskWithTagAndCommentTx(TaskCompositeRequest request);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}
