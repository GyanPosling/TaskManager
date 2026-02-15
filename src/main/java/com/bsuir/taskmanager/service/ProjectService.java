package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.ProjectRequest;
import com.bsuir.taskmanager.dto.response.ProjectResponse;
import java.util.List;

public interface ProjectService {
    List<ProjectResponse> findAll();

    ProjectResponse findById(Long id);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse update(Long id, ProjectRequest request);

    void delete(Long id);
}
