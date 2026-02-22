package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Users", description = "User CRUD operations")
public interface UserControllerApi {
    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users returned")
    ResponseEntity<List<UserResponse>> getAll();

    @Operation(summary = "Get user by id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserResponse> getById(Long id);

    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    ResponseEntity<UserResponse> create(UserRequest request);

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserResponse> update(Long id, UserRequest request);

    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<Void> delete(Long id);
}
