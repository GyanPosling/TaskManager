package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.dto.request.ProjectRequest;
import com.bsuir.taskmanager.dto.response.ProjectResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setOwnerId(project.getOwner() != null ? project.getOwner().getId() : null);
        return response;
    }

    public Project fromRequest(ProjectRequest request, User owner) {
        if (request == null) {
            return null;
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        return project;
    }

    public ProjectRequest toRequest(Project project) {
        if (project == null) {
            return null;
        }

        ProjectRequest request = new ProjectRequest();
        request.setName(project.getName());
        request.setDescription(project.getDescription());
        request.setOwnerId(project.getOwner() != null ? project.getOwner().getId() : null);
        return request;
    }
}
