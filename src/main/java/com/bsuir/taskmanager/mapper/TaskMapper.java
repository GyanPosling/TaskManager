package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.dto.request.TaskRequest;
import com.bsuir.taskmanager.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.model.entity.User;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        response.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        response.setTagIds(mapTagIds(task.getTags()));
        return response;
    }

    public Task fromRequest(TaskRequest request, Project project, User assignee, Set<Tag> tags) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;
        task.setStatus(status);
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setTags(tags != null ? tags : new HashSet<>());
        return task;
    }


    public TaskRequest toRequest(Task task) {
        if (task == null) {
            return null;
        }

        TaskRequest request = new TaskRequest();
        request.setTitle(task.getTitle());
        request.setDescription(task.getDescription());
        request.setStatus(task.getStatus());
        request.setDueDate(task.getDueDate());
        request.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        request.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        request.setTagIds(mapTagIds(task.getTags()));
        return request;
    }

    private Set<Long> mapTagIds(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        Set<Long> tagIds = new HashSet<>();
        for (Tag tag : tags) {
            tagIds.add(tag.getId());
        }
        return tagIds;
    }
}
