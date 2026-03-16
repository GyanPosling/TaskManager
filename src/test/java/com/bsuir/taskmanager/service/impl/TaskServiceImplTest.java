package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.cache.TaskSearchQueryKey;
import com.bsuir.taskmanager.exception.BulkTaskCreationException;
import com.bsuir.taskmanager.exception.ProjectNotFoundException;
import com.bsuir.taskmanager.exception.TaskNotFoundException;
import com.bsuir.taskmanager.exception.TagsNotFoundException;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskSearchCache taskSearchCache;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(
                taskRepository,
                projectRepository,
                userRepository,
                tagRepository,
                taskMapper,
                taskSearchCache
        );
    }

    @Test
    void findAllShouldMapTasks() {
        Task task = task(10L, "Backlog task");
        TaskResponse response = taskResponse(10L, "Backlog task");

        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        List<TaskResponse> result = taskService.findAll();

        assertEquals(List.of(response), result);
    }

    @Test
    void findAllWithTagsShouldMapTasks() {
        Task task = task(11L, "Tagged task");
        TaskResponse response = taskResponse(11L, "Tagged task");

        when(taskRepository.findAllWithTags()).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        List<TaskResponse> result = taskService.findAllWithTags();

        assertEquals(List.of(response), result);
    }

    @Test
    void findAllWithCommentsShouldMapTasks() {
        Task task = task(12L, "Discussed task");
        TaskResponse response = taskResponse(12L, "Discussed task");

        when(taskRepository.findAllWithComments()).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        List<TaskResponse> result = taskService.findAllWithComments();

        assertEquals(List.of(response), result);
    }

    @Test
    void findByIdShouldReturnMappedTask() {
        Task task = task(13L, "Single task");
        TaskResponse response = taskResponse(13L, "Single task");

        when(taskRepository.findById(13L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.findById(13L);

        assertSame(response, result);
    }

    @Test
    void findByIdShouldThrowWhenTaskMissing() {
        when(taskRepository.findById(13L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.findById(13L));
    }

    @Test
    void findByStatusShouldMapTasks() {
        Task task = task(14L, "Todo task");
        TaskResponse response = taskResponse(14L, "Todo task");

        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        List<TaskResponse> result = taskService.findByStatus(TaskStatus.TODO);

        assertEquals(List.of(response), result);
    }

    @Test
    void findByProjectOwnerAndStatusShouldLoadAndCachePageWhenCacheMisses() {
        PageRequest pageable = PageRequest.of(0, 5);
        Task task = task(30L, "Loaded task");
        TaskResponse response = taskResponse(30L, "Loaded task");
        Page<Task> tasksPage = new PageImpl<>(List.of(task), pageable, 1);
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forProjectOwnerAndStatus(
                9L,
                TaskStatus.TODO,
                pageable
        );

        when(taskSearchCache.get(cacheKey)).thenReturn(Optional.empty());
        when(taskRepository.findByProjectOwnerIdAndStatus(9L, TaskStatus.TODO, pageable))
                .thenReturn(tasksPage);
        when(taskMapper.toResponse(task)).thenReturn(response);

        Page<TaskResponse> result = taskService.findByProjectOwnerAndStatus(
                9L,
                TaskStatus.TODO,
                pageable
        );

        assertEquals(List.of(response), result.getContent());
        verify(taskSearchCache).put(cacheKey, result);
    }

    @Test
    void findByTagNameAndDueDateJpqlShouldReturnCachedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        LocalDate dueDate = LocalDate.of(2026, 3, 20);
        TaskResponse response = taskResponse(40L, "Cached tag task");
        Page<TaskResponse> cachedPage = new PageImpl<>(List.of(response), pageable, 1);
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forTagAndDueDateJpql(
                "backend",
                dueDate,
                pageable
        );

        when(taskSearchCache.get(cacheKey)).thenReturn(Optional.of(cachedPage));

        Page<TaskResponse> result = taskService.findByTagNameAndDueDateJpql(
                "backend",
                dueDate,
                pageable
        );

        assertSame(cachedPage, result);
    }

    @Test
    void findByTagNameAndDueDateJpqlShouldLoadAndCachePageWhenCacheMisses() {
        PageRequest pageable = PageRequest.of(0, 5);
        LocalDate dueDate = LocalDate.of(2026, 3, 20);
        Task task = task(41L, "JPQL task");
        TaskResponse response = taskResponse(41L, "JPQL task");
        Page<Task> tasksPage = new PageImpl<>(List.of(task), pageable, 1);
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forTagAndDueDateJpql(
                "backend",
                dueDate,
                pageable
        );

        when(taskSearchCache.get(cacheKey)).thenReturn(Optional.empty());
        when(taskRepository.findByTagNameAndDueDateBeforeEqualJpql("backend", dueDate, pageable))
                .thenReturn(tasksPage);
        when(taskMapper.toResponse(task)).thenReturn(response);

        Page<TaskResponse> result = taskService.findByTagNameAndDueDateJpql(
                "backend",
                dueDate,
                pageable
        );

        assertEquals(List.of(response), result.getContent());
        verify(taskSearchCache).put(cacheKey, result);
    }

    @Test
    void findByTagNameAndDueDateNativeShouldLoadAndCachePageWhenCacheMisses() {
        PageRequest pageable = PageRequest.of(0, 5);
        LocalDate dueDate = LocalDate.of(2026, 3, 20);
        Task task = task(42L, "Native task");
        TaskResponse response = taskResponse(42L, "Native task");
        Page<Task> tasksPage = new PageImpl<>(List.of(task), pageable, 1);
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forTagAndDueDateNative(
                "backend",
                dueDate,
                pageable
        );

        when(taskSearchCache.get(cacheKey)).thenReturn(Optional.empty());
        when(taskRepository.findByTagNameAndDueDateBeforeEqualNative("backend", dueDate, pageable))
                .thenReturn(tasksPage);
        when(taskMapper.toResponse(task)).thenReturn(response);

        Page<TaskResponse> result = taskService.findByTagNameAndDueDateNative(
                "backend",
                dueDate,
                pageable
        );

        assertEquals(List.of(response), result.getContent());
        verify(taskSearchCache).put(cacheKey, result);
    }

    @Test
    void createShouldThrowWhenProjectMissing() {
        TaskRequest request = taskRequest("Assigned task", 1L, 5L, Set.of());

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> taskService.create(request));
    }

    @Test
    void createShouldThrowWhenAssigneeMissing() {
        Project project = project(1L);
        TaskRequest request = taskRequest("Assigned task", 1L, 5L, Set.of());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> taskService.create(request));
    }

    @Test
    void createBulkNoTxShouldCreateAllTasksAndInvalidateCache() {
        Project project = project(1L);
        User assignee = user(3L);

        TaskRequest firstRequest = taskRequest("Backlog task", 1L, null, Set.of());
        TaskRequest secondRequest = taskRequest("Sprint task", 1L, 3L, Set.of());

        Task firstTask = task(11L, "Backlog task");
        Task secondTask = task(12L, "Sprint task");
        TaskResponse firstResponse = taskResponse(11L, "Backlog task");
        TaskResponse secondResponse = taskResponse(12L, "Sprint task");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(3L)).thenReturn(Optional.of(assignee));
        when(taskMapper.fromRequest(firstRequest, project, null, Set.of())).thenReturn(firstTask);
        when(taskMapper.fromRequest(secondRequest, project, assignee, Set.of())).thenReturn(secondTask);
        when(taskRepository.save(firstTask)).thenReturn(firstTask);
        when(taskRepository.save(secondTask)).thenReturn(secondTask);
        when(taskMapper.toResponse(firstTask)).thenReturn(firstResponse);
        when(taskMapper.toResponse(secondTask)).thenReturn(secondResponse);

        List<TaskResponse> result = taskService.createBulkNoTx(
                List.of(firstRequest, secondRequest),
                null
        );

        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(taskSearchCache).clear();
    }

    @Test
    void createBulkNoTxShouldThrowAfterConfiguredIndexAndClearCache() {
        Project project = project(1L);
        TaskRequest request = taskRequest("Failing task", 1L, null, Set.of());
        Task savedTask = task(21L, "Failing task");
        List<TaskRequest> requests = List.of(request, request);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.fromRequest(request, project, null, Set.of())).thenReturn(savedTask);
        when(taskRepository.save(savedTask)).thenReturn(savedTask);

        BulkTaskCreationException exception = assertThrows(
                BulkTaskCreationException.class,
                () -> taskService.createBulkNoTx(requests, 1)
        );

        assertEquals("Forced error after saving 1 bulk tasks", exception.getMessage());
        verify(taskRepository).save(savedTask);
        verify(taskMapper, never()).toResponse(savedTask);
        verify(taskSearchCache).clear();
    }

    @Test
    void createBulkTxShouldThrowWhenNotAllTagsExist() {
        Project project = project(1L);
        TaskRequest request = taskRequest("Tagged task", 1L, null, Set.of(4L, 5L));
        List<TaskRequest> requests = List.of(request);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(tagRepository.findAllById(Set.of(4L, 5L))).thenReturn(List.of(tag(4L)));

        assertThrows(
                TagsNotFoundException.class,
                () -> taskService.createBulkTx(requests, null)
        );

        verify(taskRepository, never()).save(any(Task.class));
        verify(taskSearchCache).clear();
    }

    @Test
    void createBulkTxShouldCreateAllTasksAndInvalidateCache() {
        Project project = project(1L);
        TaskRequest request = taskRequest("Transactional task", 1L, null, Set.of());
        Task savedTask = task(23L, "Transactional task");
        TaskResponse response = taskResponse(23L, "Transactional task");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.fromRequest(request, project, null, Set.of())).thenReturn(savedTask);
        when(taskRepository.save(savedTask)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(response);

        List<TaskResponse> result = taskService.createBulkTx(List.of(request), 2);

        assertEquals(List.of(response), result);
        verify(taskSearchCache).clear();
    }

    @Test
    void findByProjectOwnerAndStatusShouldReturnCachedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        TaskResponse response = taskResponse(31L, "Cached task");
        Page<TaskResponse> cachedPage = new PageImpl<>(List.of(response), pageable, 1);
        TaskSearchQueryKey cacheKey = TaskSearchQueryKey.forProjectOwnerAndStatus(
                9L,
                TaskStatus.TODO,
                pageable
        );

        when(taskSearchCache.get(cacheKey)).thenReturn(Optional.of(cachedPage));

        Page<TaskResponse> result = taskService.findByProjectOwnerAndStatus(
                9L,
                TaskStatus.TODO,
                pageable
        );

        assertSame(cachedPage, result);
        verify(taskRepository, never()).findByProjectOwnerIdAndStatus(9L, TaskStatus.TODO, pageable);
    }

    @Test
    void createShouldResolveOptionalAssignee() {
        Project project = project(1L);
        User assignee = user(5L);
        Tag firstTag = tag(7L);
        Tag secondTag = tag(8L);
        Set<Long> tagIds = Set.of(7L, 8L);
        TaskRequest request = taskRequest("Assigned task", 1L, 5L, tagIds);
        Task mappedTask = task(41L, "Assigned task");
        TaskResponse response = taskResponse(41L, "Assigned task");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(tagIds)).thenReturn(List.of(firstTag, secondTag));
        when(taskMapper.fromRequest(eq(request), eq(project), eq(assignee), anySet()))
                .thenReturn(mappedTask);
        when(taskRepository.save(mappedTask)).thenReturn(mappedTask);
        when(taskMapper.toResponse(mappedTask)).thenReturn(response);

        TaskResponse result = taskService.create(request);

        assertSame(response, result);
        verify(taskSearchCache).clear();
    }

    @Test
    void updateShouldSaveTaskAndClearCache() {
        Project project = project(1L);
        User assignee = user(5L);
        Tag firstTag = tag(7L);
        Tag secondTag = tag(8L);
        Set<Long> tagIds = Set.of(7L, 8L);
        TaskRequest request = taskRequest("Updated task", 1L, 5L, tagIds);
        Task existingTask = task(51L, "Old task");
        TaskResponse response = taskResponse(51L, "Updated task");

        when(taskRepository.findById(51L)).thenReturn(Optional.of(existingTask));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(tagIds)).thenReturn(List.of(firstTag, secondTag));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(response);

        TaskResponse result = taskService.update(51L, request);

        assertSame(response, result);
        assertSame(project, existingTask.getProject());
        assertSame(assignee, existingTask.getAssignee());
        verify(taskSearchCache).clear();
    }

    @Test
    void updateShouldThrowWhenTaskMissing() {
        TaskRequest request = taskRequest("Updated task", 1L, 5L, Set.of());

        when(taskRepository.findById(51L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.update(51L, request));
    }

    @Test
    void deleteShouldRemoveTaskAndClearCache() {
        when(taskRepository.existsById(61L)).thenReturn(true);

        taskService.delete(61L);

        verify(taskRepository).deleteById(61L);
        verify(taskSearchCache).clear();
    }

    @Test
    void deleteShouldThrowWhenTaskMissing() {
        when(taskRepository.existsById(61L)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.delete(61L));
    }

    private TaskRequest taskRequest(
            String title,
            Long projectId,
            Long assigneeId,
            Set<Long> tagIds
    ) {
        return new TaskRequest(
                title,
                "Description for " + title,
                TaskStatus.TODO,
                LocalDate.now().plusDays(1),
                projectId,
                assigneeId,
                tagIds
        );
    }

    private Project project(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Project " + id);
        return project;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        return user;
    }

    private Tag tag(Long id) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("tag-" + id);
        return tag;
    }

    private Task task(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(TaskStatus.TODO);
        return task;
    }

    private TaskResponse taskResponse(Long id, String title) {
        return new TaskResponse(id, title, null, TaskStatus.TODO, null, 1L, null, Set.of());
    }
}
