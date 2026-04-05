package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.CommentRequest;
import com.bsuir.taskmanager.model.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Comments", description = "Comment CRUD operations")
@UnauthorizedApiResponse
public interface CommentControllerApi {
    @Operation(summary = "Get all comments")
    @ApiResponse(responseCode = "200", description = "Comments returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<CommentResponse>> getAll();

    @Operation(summary = "Get comment by id")
    @ApiResponse(responseCode = "200", description = "Comment found")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<CommentResponse> getById(@Positive Long id);

    @Operation(summary = "Create comment")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<CommentResponse> create(@Valid CommentRequest request);

    @Operation(summary = "Update comment")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<CommentResponse> update(@Positive Long id, @Valid CommentRequest request);

    @Operation(summary = "Delete comment")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Void> delete(@Positive Long id);
}
