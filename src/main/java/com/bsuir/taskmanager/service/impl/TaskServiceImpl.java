package com.bsuir.taskmanager.service.impl;

import com.bsuir.taskmanager.cache.CacheKey;
import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.exception.FailAfterTaskException;
import com.bsuir.taskmanager.exception.ProjectNotFoundException;
import com.bsuir.taskmanager.exception.TagsNotFoundException;
import com.bsuir.taskmanager.exception.TaskNotFoundException;
import com.bsuir.taskmanager.exception.UserNotFoundException;
import com.bsuir.taskmanager.mapper.TaskMapper;
import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.Comment;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.CommentRepository;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.TagRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import com.bsuir.taskmanager.service.TaskService;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
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
    public Page<TaskResponse> findByProjectOwnerAndStatus(Long ownerId, TaskStatus status, Pageable pageable) {
        CacheKey cacheKey = new CacheKey(
                Task.class,
                "findByProjectOwnerAndStatus",
                ownerId,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString()
        );
        Page<TaskResponse> cachedPage = taskSearchCache.get(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }
        Page<TaskResponse> responsePage = taskRepository.findByProjectOwnerIdAndStatus(ownerId, status, pageable)
                .map(taskMapper::toResponse);
        taskSearchCache.put(cacheKey, responsePage);
        return responsePage;
    }

    @Override
    public Page<TaskResponse> findByTagNameAndDueDate(String tagName, LocalDate dueDate, Pageable pageable) {
        CacheKey cacheKey = new CacheKey(
                Task.class,
                "findByTagNameAndDueDate",
                tagName,
                dueDate,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString()
        );
        Page<TaskResponse> cachedPage = taskSearchCache.get(cacheKey);
        if (cachedPage != null) {   
            return cachedPage;
        }
        Page<TaskResponse> responsePage = taskRepository.findByTagNameAndDueDateBeforeEqual(tagName, dueDate, pageable)
                .map(taskMapper::toResponse);
        taskSearchCache.put(cacheKey, responsePage);
        return responsePage;
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
    public TaskResponse createTaskWithTagAndCommentNoTx(TaskCompositeRequest request) {
        return createCompositeInternal(request);
    }

    @Override
    @Transactional
    public TaskResponse createTaskWithTagAndCommentTx(TaskCompositeRequest request) {
        return createCompositeInternal(request);
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
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + assigneeId));
    }

    private Set<Tag> getTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new TagsNotFoundException("Some tags not found");
        }
        return new HashSet<>(tags);
    }

    private User getCommentAuthor(Long authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + authorId));
    }

    private TaskResponse createCompositeInternal(TaskCompositeRequest request) {
        Project project = getProject(request.getProjectId());
        User assignee = getAssignee(request.getAssigneeId());
        Task task = taskMapper.fromRequest(request, project, assignee, new HashSet<>());
        Task savedTask = taskRepository.save(task);

        Tag tag = new Tag();
        tag.setName(request.getTagName());
        Tag savedTag = tagRepository.save(tag);
        savedTask.getTags().add(savedTag);
        savedTask = taskRepository.save(savedTask);
        invalidateSearchCache();

        if (request.isFailAfterTask()) {
            throw new FailAfterTaskException("Forced error after saving task and tag");
        }

        User author = getCommentAuthor(request.getCommentAuthorId());
        Comment comment = new Comment();
        comment.setText(request.getCommentText());
        comment.setTask(savedTask);
        comment.setAuthor(author);
        commentRepository.save(comment);
        return taskMapper.toResponse(savedTask);
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    private void invalidateSearchCache() {
        taskSearchCache.clear();
    }
}
