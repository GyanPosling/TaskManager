package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.TaskCompositeRequest;
import com.bsuir.taskmanager.model.dto.request.TaskRequest;
import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import com.bsuir.taskmanager.model.entity.User;
import java.util.HashSet;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", imports = {HashSet.class, TaskStatus.class})
public interface TaskMapper {
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "tagIds", source = "tags", qualifiedByName = "mapTagIds")
    TaskResponse toResponse(Task task);

    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "tags", expression = "java(tags != null ? tags : new HashSet<>())")
    @Mapping(target = "status", expression = "java(request.getStatus() != null"
            + " ? request.getStatus() : TaskStatus.TODO)")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Task fromRequest(TaskRequest request, Project project, User assignee, Set<Tag> tags);

    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "tags", expression = "java(tags != null ? tags : new HashSet<>())")
    @Mapping(target = "status", expression = "java(request.getStatus() != null"
            + " ? request.getStatus() : TaskStatus.TODO)")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Task fromRequest(TaskCompositeRequest request, Project project, User assignee, Set<Tag> tags);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "tagIds", source = "tags", qualifiedByName = "mapTagIds")
    TaskRequest toRequest(Task task);

    @Named("mapTagIds")
    default Set<Long> mapTagIds(Set<Tag> tags) {
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
