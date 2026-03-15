package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.exception.AsyncBulkTaskOperationNotFoundException;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatus;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskAsyncServiceImplTest {
    @Mock
    private TaskAsyncWorker taskAsyncWorker;

    private TaskAsyncServiceImpl taskAsyncService;

    @BeforeEach
    void setUp() {
        taskAsyncService = new TaskAsyncServiceImpl(taskAsyncWorker);
    }

    @Test
    void startBulkCreateShouldReturnPendingOperationStatus() {
        List<TaskRequest> requests = List.of(taskRequest("Async task"));

        when(taskAsyncWorker.processBulkCreate(
                any(AsyncBulkTaskOperation.class),
                eq(requests)
        )).thenReturn(CompletableFuture.completedFuture(null));

        AsyncBulkTaskStatusResponse response = taskAsyncService.startBulkCreate(requests);

        assertTrue(response.getOperationId() > 0);
        assertEquals(AsyncBulkTaskStatus.PENDING, response.getStatus());
        verify(taskAsyncWorker).processBulkCreate(
                any(AsyncBulkTaskOperation.class),
                eq(requests)
        );
    }

    @Test
    void getBulkCreateStatusShouldThrowWhenOperationMissing() {
        assertThrows(
                AsyncBulkTaskOperationNotFoundException.class,
                () -> taskAsyncService.getBulkCreateStatus(99L)
        );
    }

    private TaskRequest taskRequest(String title) {
        return new TaskRequest(
                title,
                "Description for " + title,
                TaskStatus.TODO,
                LocalDate.now().plusDays(1),
                1L,
                1L,
                Set.of(1L)
        );
    }
}
