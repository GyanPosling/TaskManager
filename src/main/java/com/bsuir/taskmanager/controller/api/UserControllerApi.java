package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Users", description = "User CRUD operations")
@UnauthorizedApiResponse
public interface UserControllerApi {
    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users returned")
    @InternalServerErrorApiResponse
    ResponseEntity<List<UserResponse>> getAll();

    @Operation(summary = "Get user by id")
    @ApiResponse(responseCode = "200", description = "User found")
    @NotFoundApiResponse
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<UserResponse> getById(@Positive Long id);

    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "201", description = "User created")
    @BadRequestApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<UserResponse> create(@Valid UserRequest request);

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<UserResponse> update(@Positive Long id, @Valid UserRequest request);

    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @BadRequestApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<Void> delete(@Positive Long id);
}
