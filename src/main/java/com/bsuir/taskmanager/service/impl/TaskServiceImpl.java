package com.bsuir.taskmanager.service.impl;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.cache.TaskSearchQueryKey;
import com.bsuir.taskmanager.exception.BulkTaskCreationException;
import com.bsuir.taskmanager.exception.ProjectNotFoundException;
import com.bsuir.taskmanager.exception.TagsNotFoundException;
import com.bsuir.taskmanager.exception.TaskNotFoundException;
import com.bsuir.taskmanager.exception.UserNotFoundException;
import com.bsuir.taskmanager.mapper.TaskMapper;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.TagRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import com.bsuir.taskmanager.service.TaskService;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private static final String TASK_SEARCH_CACHE_HIT_LOG =
            "Task search cache hit: queryType={}, "
                    + "tagName={}, dueDate={}, page={}, size={}, sort={}";
    private static final String TASK_SEARCH_CACHE_MISS_LOG =
            "Task search cache miss: queryType={}, "
                    + "tagName={}, dueDate={}, page={}, size={}, sort={}";

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final TaskSearchCache taskSearchCache;

    @Override
    public List<TaskResponse> findAll() {
        return toResponses(taskRepository.findAll());
    }

    @Override
    public List<TaskResponse> findAllWithTags() {
        return toResponses(taskRepository.findAllWithTags());
    }

    @Override
    public List<TaskResponse> findAllWithComments() {
        return toResponses(taskRepository.findAllWithComments());
    }

    @Override
    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> findByStatus(TaskStatus status) {
        return toResponses(taskRepository.findByStatus(status));
    }

    @Override
    public Page<TaskResponse> findByProjectOwnerAndStatus(
            Long ownerId,
            TaskStatus status,
            Pageable pageable
    ) {
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forProjectOwnerAndStatus(
                ownerId,
                status,
                pageable
        );
        return taskSearchCache.get(cacheKey)
                .map(cachedPage -> {
                    logCacheHit(cacheKey);
                    return cachedPage;
                })
                .orElseGet(() -> {
                    logCacheMiss(cacheKey);
                    Page<TaskResponse> responsePage = taskRepository
                            .findByProjectOwnerIdAndStatus(ownerId, status, pageable)
                            .map(taskMapper::toResponse);
                    taskSearchCache.put(cacheKey, responsePage);
                    return responsePage;
                });
    }

    @Override
    public Page<TaskResponse> findByTagNameAndDueDateJpql(
            String tagName,
            LocalDate dueDate,
            Pageable pageable
    ) {
        return findByTagNameAndDueDate(
                TaskSearchQueryKey.forTagAndDueDateJpql(tagName, dueDate, pageable),
                () -> taskRepository.findByTagNameAndDueDateBeforeEqualJpql(
                        tagName,
                        dueDate,
                        pageable
                )
        );
    }

    @Override
    public Page<TaskResponse> findByTagNameAndDueDateNative(
            String tagName,
            LocalDate dueDate,
            Pageable pageable
    ) {
        return findByTagNameAndDueDate(
                TaskSearchQueryKey.forTagAndDueDateNative(tagName, dueDate, pageable),
                () -> taskRepository.findByTagNameAndDueDateBeforeEqualNative(
                        tagName,
                        dueDate,
                        pageable
                )
        );
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Set<Tag> tags = getTags(request.getTagIds());
        Task task = taskMapper.fromRequest(request, project, assignee, tags);
        Task saved = taskRepository.save(task);
        invalidateSearchCache();
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<TaskResponse> createBulkNoTx(List<TaskRequest> requests, Integer failAfterIndex) {
        return createBulkInternal(requests, failAfterIndex);
    }

    @Override
    @Transactional
    public List<TaskResponse> createBulkTx(List<TaskRequest> requests, Integer failAfterIndex) {
        return createBulkInternal(requests, failAfterIndex);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Set<Tag> tags = getTags(request.getTagIds());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setTags(tags);
        Task saved = taskRepository.save(task);
        invalidateSearchCache();
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
        invalidateSearchCache();
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

    private Page<TaskResponse> findByTagNameAndDueDate(
            TaskSearchQueryKey cacheKey,
            TaskPageSupplier taskPageSupplier
    ) {
        return taskSearchCache.get(cacheKey)
                .map(cachedPage -> {
                    logCacheHit(cacheKey);
                    return cachedPage;
                })
                .orElseGet(() -> {
                    logCacheMiss(cacheKey);
                    Page<TaskResponse> responsePage = taskPageSupplier.get()
                            .map(taskMapper::toResponse);
                    taskSearchCache.put(cacheKey, responsePage);
                    return responsePage;
                });
    }

    private List<TaskResponse> createBulkInternal(
            List<TaskRequest> requests,
            Integer failAfterIndex
    ) {
        try {
            return IntStream.range(0, requests.size())
                    .mapToObj(index -> createBulkTask(
                            requests.get(index),
                            index + 1,
                            failAfterIndex
                    ))
                    .toList();
        } finally {
            invalidateSearchCache();
        }
    }

    private TaskResponse createBulkTask(
            TaskRequest request,
            int processedTasksCount,
            Integer failAfterIndex
    ) {
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Set<Tag> tags = getTags(request.getTagIds());
        Task task = taskMapper.fromRequest(request, project, assignee, tags);
        Task savedTask = taskRepository.save(task);

        if (shouldFailAfterTask(processedTasksCount, failAfterIndex)) {
            throw new BulkTaskCreationException(
                    "Forced error after saving %d bulk tasks".formatted(processedTasksCount)
            );
        }

        return taskMapper.toResponse(savedTask);
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    private Set<Tag> loadTags(Set<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new TagsNotFoundException("Some tags not found");
        }
        return new HashSet<>(tags);
    }

    private boolean shouldFailAfterTask(int processedTasksCount, Integer failAfterIndex) {
        return Optional.ofNullable(failAfterIndex)
                .filter(index -> index == processedTasksCount)
                .isPresent();
    }

    private void logCacheHit(TaskSearchQueryKey cacheKey) {
        log.info(
                TASK_SEARCH_CACHE_HIT_LOG,
                cacheKey.queryType(),
                cacheKey.tagName(),
                cacheKey.dueDate(),
                cacheKey.pageNumber(),
                cacheKey.pageSize(),
                cacheKey.sort()
        );
    }

    private void logCacheMiss(TaskSearchQueryKey cacheKey) {
        log.info(
                TASK_SEARCH_CACHE_MISS_LOG,
                cacheKey.queryType(),
                cacheKey.tagName(),
                cacheKey.dueDate(),
                cacheKey.pageNumber(),
                cacheKey.pageSize(),
                cacheKey.sort()
        );
    }

    private void invalidateSearchCache() {
        taskSearchCache.clear();
    }

    @FunctionalInterface
    private interface TaskPageSupplier {
        Page<Task> get();
    }
}
