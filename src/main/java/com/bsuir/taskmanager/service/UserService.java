package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.UserRequest;
import com.bsuir.taskmanager.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);
}
