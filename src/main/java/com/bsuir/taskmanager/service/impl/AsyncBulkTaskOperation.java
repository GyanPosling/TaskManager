package com.bsuir.taskmanager.service.impl;

import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatus;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class AsyncBulkTaskOperation {
    private final Long operationId;
    private final AtomicInteger processedTasks;
    private final AtomicInteger createdTasks;
    private final List<Long> createdTaskIds;
    private volatile AsyncBulkTaskStatus status;
    private volatile String errorMessage;

    public AsyncBulkTaskOperation(Long operationId) {
        this.operationId = operationId;
        this.processedTasks = new AtomicInteger();
        this.createdTasks = new AtomicInteger();
        this.createdTaskIds = Collections.synchronizedList(new ArrayList<>());
        this.status = AsyncBulkTaskStatus.PENDING;
        this.errorMessage = null;
    }

    void markInProgress() {
        status = AsyncBulkTaskStatus.IN_PROGRESS;
    }

    void recordCreatedTask(Long taskId) {
        processedTasks.incrementAndGet();
        createdTasks.incrementAndGet();
        createdTaskIds.add(taskId);
    }

    void markCompleted() {
        status = AsyncBulkTaskStatus.COMPLETED;
    }

    void markFailed(String message) {
        status = AsyncBulkTaskStatus.FAILED;
        errorMessage = message;
    }

    AsyncBulkTaskStatusResponse toResponse() {
        synchronized (createdTaskIds) {
            return new AsyncBulkTaskStatusResponse(
                    operationId,
                    status,
                    processedTasks.get(),
                    createdTasks.get(),
                    new ArrayList<>(createdTaskIds),
                    errorMessage
            );
        }
    }
}
