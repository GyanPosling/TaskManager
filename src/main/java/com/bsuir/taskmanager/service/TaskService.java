package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.TaskRequest;
import com.bsuir.taskmanager.dto.response.TaskResponse;
import com.bsuir.taskmanager.mapper.TaskMapper;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.TagRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;

    public List<TaskResponse> findAll() {
        return toResponses(taskRepository.findAll());
    }

    public List<TaskResponse> findAllWithTags() {
        return toResponses(taskRepository.findAllWithTags());
    }

    public List<TaskResponse> findAllWithComments() {
        return toResponses(taskRepository.findAllWithComments());
    }

    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
        return taskMapper.toResponse(task);
    }

    public List<TaskResponse> findByStatus(TaskStatus status) {
        return toResponses(taskRepository.findByStatus(status));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Set<Tag> tags = getTags(request.getTagIds());
        Task task = taskMapper.fromRequest(request, project, assignee, tags);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
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
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
    }

    private User getAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + assigneeId));
    }

    private Set<Tag> getTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new EntityNotFoundException("Some tags not found");
        }
        return new HashSet<>(tags);
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }
}
