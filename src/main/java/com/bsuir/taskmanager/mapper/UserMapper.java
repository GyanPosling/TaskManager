package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.dto.request.UserRequest;
import com.bsuir.taskmanager.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }

    public User fromRequest(UserRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        return user;
    }

    public UserRequest toRequest(User user) {
        if (user == null) {
            return null;
        }

        UserRequest request = new UserRequest();
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        return request;
    }
}
