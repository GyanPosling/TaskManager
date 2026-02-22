package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.CommentRequest;
import com.bsuir.taskmanager.model.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Comments", description = "Comment CRUD operations")
public interface CommentControllerApi {
    @Operation(summary = "Get all comments")
    @ApiResponse(responseCode = "200", description = "Comments returned")
    ResponseEntity<List<CommentResponse>> getAll();

    @Operation(summary = "Get comment by id")
    @ApiResponse(responseCode = "200", description = "Comment found")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<CommentResponse> getById(Long id);

    @Operation(summary = "Create comment")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<CommentResponse> create(CommentRequest request);

    @Operation(summary = "Update comment")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<CommentResponse> update(Long id, CommentRequest request);

    @Operation(summary = "Delete comment")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    ResponseEntity<Void> delete(Long id);
}
