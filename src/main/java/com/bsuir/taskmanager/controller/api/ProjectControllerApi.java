package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.ProjectRequest;
import com.bsuir.taskmanager.model.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Projects", description = "Project CRUD operations")
public interface ProjectControllerApi {
    @Operation(summary = "Get all projects")
    @ApiResponse(responseCode = "200", description = "Projects returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<ProjectResponse>> getAll();

    @Operation(summary = "Get project by id")
    @ApiResponse(responseCode = "200", description = "Project found")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<ProjectResponse> getById(@Positive Long id);

    @Operation(summary = "Create project")
    @ApiResponse(responseCode = "201", description = "Project created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<ProjectResponse> create(@Valid ProjectRequest request);

    @Operation(summary = "Update project")
    @ApiResponse(responseCode = "200", description = "Project updated")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<ProjectResponse> update(@Positive Long id, @Valid ProjectRequest request);

    @Operation(summary = "Delete project")
    @ApiResponse(responseCode = "204", description = "Project deleted")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Void> delete(@Positive Long id);
}
