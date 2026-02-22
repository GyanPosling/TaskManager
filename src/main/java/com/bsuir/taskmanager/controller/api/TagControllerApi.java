package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.TagRequest;
import com.bsuir.taskmanager.model.dto.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Tags", description = "Tag CRUD operations")
public interface TagControllerApi {
    @Operation(summary = "Get all tags")
    @ApiResponse(responseCode = "200", description = "Tags returned")
    ResponseEntity<List<TagResponse>> getAll();

    @Operation(summary = "Get tag by id")
    @ApiResponse(responseCode = "200", description = "Tag found")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    ResponseEntity<TagResponse> getById(Long id);

    @Operation(summary = "Create tag")
    @ApiResponse(responseCode = "201", description = "Tag created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<TagResponse> create(TagRequest request);

    @Operation(summary = "Update tag")
    @ApiResponse(responseCode = "200", description = "Tag updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    ResponseEntity<TagResponse> update(Long id, TagRequest request);

    @Operation(summary = "Delete tag")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    ResponseEntity<Void> delete(Long id);
}
