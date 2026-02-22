package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.ProjectRequest;
import com.bsuir.taskmanager.model.dto.response.ProjectResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    ProjectResponse toResponse(Project project);

    @Mapping(target = "owner", source = "owner")
    Project fromRequest(ProjectRequest request, User owner);

    @Mapping(target = "ownerId", source = "owner.id")
    ProjectRequest toRequest(Project project);
}
