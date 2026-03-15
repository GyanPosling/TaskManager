package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.mapper.TaskMapper;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatus;
import com.bsuir.taskmanager.model.dto.response.AsyncBulkTaskStatusResponse;
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

@ExtendWith(MockitoExtension.class)
class TaskAsyncWorkerTest {
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

    private TaskAsyncWorker taskAsyncWorker;

    @BeforeEach
    void setUp() {
        taskAsyncWorker = new TaskAsyncWorker(
                taskRepository,
                projectRepository,
                userRepository,
                tagRepository,
                taskMapper,
                taskSearchCache
        );
    }

    @Test
    void processBulkCreateShouldMarkOperationCompleted() {
        AsyncBulkTaskOperation operation = new AsyncBulkTaskOperation(1L);
        TaskRequest firstRequest = taskRequest("Async first");
        TaskRequest secondRequest = taskRequest("Async second");
        List<TaskRequest> requests = List.of(firstRequest, secondRequest);

        Project project = project(1L);
        User assignee = user(1L);
        Tag tag = tag(1L);
        Task firstTask = task(10L, "Async first");
        Task secondTask = task(11L, "Async second");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(1L))).thenReturn(List.of(tag));
        when(taskMapper.fromRequest(firstRequest, project, assignee, Set.of(tag)))
                .thenReturn(firstTask);
        when(taskMapper.fromRequest(secondRequest, project, assignee, Set.of(tag)))
                .thenReturn(secondTask);
        when(taskRepository.save(firstTask)).thenReturn(firstTask);
        when(taskRepository.save(secondTask)).thenReturn(secondTask);

        AsyncBulkTaskStatusResponse response = taskAsyncWorker
                .processBulkCreate(operation, requests)
                .join();

        assertEquals(AsyncBulkTaskStatus.COMPLETED, response.getStatus());
        assertEquals(2, response.getProcessedTasks());
        assertEquals(2, response.getCreatedTasks());
        assertEquals(List.of(10L, 11L), response.getCreatedTaskIds());
        assertNull(response.getErrorMessage());
        verify(taskSearchCache, times(2)).clear();
    }

    @Test
    void processBulkCreateShouldMarkOperationFailedWhenProjectIsMissing() {
        AsyncBulkTaskOperation operation = new AsyncBulkTaskOperation(2L);
        List<TaskRequest> requests = List.of(taskRequest("Broken async task"));

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        AsyncBulkTaskStatusResponse response = taskAsyncWorker
                .processBulkCreate(operation, requests)
                .join();

        assertEquals(AsyncBulkTaskStatus.FAILED, response.getStatus());
        assertEquals(0, response.getProcessedTasks());
        assertEquals(0, response.getCreatedTasks());
        assertEquals("Project not found: 1", response.getErrorMessage());
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

    private Project project(Long id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
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
}
