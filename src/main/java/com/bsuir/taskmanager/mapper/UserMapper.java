package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    User fromRequest(UserRequest request);

    UserRequest toRequest(User user);
}
