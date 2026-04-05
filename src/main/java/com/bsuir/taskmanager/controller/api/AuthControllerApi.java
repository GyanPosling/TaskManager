package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.request.AuthLoginRequest;
import com.bsuir.taskmanager.model.dto.request.AuthRegisterRequest;
import com.bsuir.taskmanager.model.dto.response.AuthResponse;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Authentication and authorization operations")
public interface AuthControllerApi {
    @Operation(
            summary = "Register user",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponse(responseCode = "201", description = "User registered")
    @BadRequestApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<UserResponse> register(@Valid AuthRegisterRequest request);

    @Operation(
            summary = "Login and issue JWT access token",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @InternalServerErrorApiResponse
    ResponseEntity<AuthResponse> login(@Valid AuthLoginRequest request);
}
