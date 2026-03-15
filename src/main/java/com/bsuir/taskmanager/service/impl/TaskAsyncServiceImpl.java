package com.bsuir.taskmanager.service.impl;

import com.bsuir.taskmanager.exception.AsyncBulkTaskOperationNotFoundException;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import com.bsuir.taskmanager.service.TaskAsyncService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAsyncServiceImpl implements TaskAsyncService {
    private final TaskAsyncWorker taskAsyncWorker;
    private final ConcurrentHashMap<Long, AsyncBulkTaskOperation> operations =
            new ConcurrentHashMap<>();
    private final AtomicLong operationIdGenerator = new AtomicLong();

    @Override
    public AsyncBulkTaskStatusResponse startBulkCreate(List<TaskRequest> requests) {
        Long operationId = operationIdGenerator.incrementAndGet();
        AsyncBulkTaskOperation operation = new AsyncBulkTaskOperation(operationId);
        operations.put(operationId, operation);
        taskAsyncWorker.processBulkCreate(operation, requests);
        return operation.toResponse();
    }

    @Override
    public AsyncBulkTaskStatusResponse getBulkCreateStatus(Long operationId) {
        return Optional.ofNullable(operations.get(operationId))
                .map(AsyncBulkTaskOperation::toResponse)
                .orElseThrow(() -> new AsyncBulkTaskOperationNotFoundException(
                        "Async bulk operation not found: " + operationId
                ));
    }
}
