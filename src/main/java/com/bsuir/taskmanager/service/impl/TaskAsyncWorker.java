package com.bsuir.taskmanager.service.impl;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.exception.ProjectNotFoundException;
import com.bsuir.taskmanager.exception.TagsNotFoundException;
import com.bsuir.taskmanager.exception.UserNotFoundException;
import com.bsuir.taskmanager.mapper.TaskMapper;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.TagRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAsyncWorker {
    private static final long BULK_TASK_PROCESSING_DELAY_MS = 5_000L;

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final TaskSearchCache taskSearchCache;

    @Async
    public CompletableFuture<AsyncBulkTaskStatusResponse> processBulkCreate(
            AsyncBulkTaskOperation operation,
            List<TaskRequest> requests
    ) {
        operation.markInProgress();
        try {
            for (TaskRequest request : requests) {
                Task savedTask = saveTask(request);
                pauseForDemo();
                operation.recordCreatedTask(savedTask.getId());
                taskSearchCache.clear();
            }
            operation.markCompleted();
        } catch (RuntimeException ex) {
            operation.markFailed(ex.getMessage());
        }
        return CompletableFuture.completedFuture(operation.toResponse());
    }

    private Task saveTask(TaskRequest request) {
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Set<Tag> tags = getTags(request.getTagIds());
        Task task = taskMapper.fromRequest(request, project, assignee, tags);
        return taskRepository.save(task);
    }

    private void pauseForDemo() {
        try {
            Thread.sleep(BULK_TASK_PROCESSING_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Async bulk task processing was interrupted", ex);
        }
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));
    }

    private User getAssignee(Long assigneeId) {
        return Optional.ofNullable(assigneeId)
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found: " + id)))
                .orElse(null);
    }

    private Set<Tag> getTags(Set<Long> tagIds) {
        return Optional.ofNullable(tagIds)
                .filter(ids -> !ids.isEmpty())
                .map(this::loadTags)
                .orElseGet(HashSet::new);
    }

    private Set<Tag> loadTags(Set<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new TagsNotFoundException("Some tags not found");
        }
        return new HashSet<>(tags);
    }
}
