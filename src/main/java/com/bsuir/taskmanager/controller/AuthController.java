package com.bsuir.taskmanager.controller;

import com.bsuir.taskmanager.controller.api.AuthControllerApi;
import com.bsuir.taskmanager.model.dto.request.AuthLoginRequest;
import com.bsuir.taskmanager.model.dto.request.AuthRegisterRequest;
import com.bsuir.taskmanager.model.dto.response.AuthResponse;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerApi {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid AuthRegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
