package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import java.util.List;

public interface TaskAsyncService {
    AsyncBulkTaskStatusResponse startBulkCreate(List<TaskRequest> requests);

    AsyncBulkTaskStatusResponse getBulkCreateStatus(Long operationId);
}
