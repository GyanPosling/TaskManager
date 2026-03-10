package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.TagRequest;
import com.bsuir.taskmanager.model.dto.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Tags", description = "Tag CRUD operations")
public interface TagControllerApi {
    @Operation(summary = "Get all tags")
    @ApiResponse(responseCode = "200", description = "Tags returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<TagResponse>> getAll();

    @Operation(summary = "Get tag by id")
    @ApiResponse(responseCode = "200", description = "Tag found")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TagResponse> getById(@Positive Long id);

    @Operation(summary = "Create tag")
    @ApiResponse(responseCode = "201", description = "Tag created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TagResponse> create(@Valid TagRequest request);

    @Operation(summary = "Update tag")
    @ApiResponse(responseCode = "200", description = "Tag updated")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<TagResponse> update(@Positive Long id, @Valid TagRequest request);

    @Operation(summary = "Delete tag")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Void> delete(@Positive Long id);
}
