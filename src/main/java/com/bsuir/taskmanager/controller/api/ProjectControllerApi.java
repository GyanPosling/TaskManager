package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.ProjectRequest;
import com.bsuir.taskmanager.model.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Projects", description = "Project CRUD operations")
public interface ProjectControllerApi {
    @Operation(summary = "Get all projects")
    @ApiResponse(responseCode = "200", description = "Projects returned")
    ResponseEntity<List<ProjectResponse>> getAll();

    @Operation(summary = "Get project by id")
    @ApiResponse(responseCode = "200", description = "Project found")
    @ApiResponse(responseCode = "404", description = "Project not found")
    ResponseEntity<ProjectResponse> getById(Long id);

    @Operation(summary = "Create project")
    @ApiResponse(responseCode = "201", description = "Project created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<ProjectResponse> create(ProjectRequest request);

    @Operation(summary = "Update project")
    @ApiResponse(responseCode = "200", description = "Project updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Project not found")
    ResponseEntity<ProjectResponse> update(Long id, ProjectRequest request);

    @Operation(summary = "Delete project")
    @ApiResponse(responseCode = "204", description = "Project deleted")
    @ApiResponse(responseCode = "404", description = "Project not found")
    ResponseEntity<Void> delete(Long id);
}
