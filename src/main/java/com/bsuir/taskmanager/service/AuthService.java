package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.model.dto.request.AuthLoginRequest;
import com.bsuir.taskmanager.model.dto.request.AuthRegisterRequest;
import com.bsuir.taskmanager.model.dto.response.AuthResponse;
import com.bsuir.taskmanager.model.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);
}
