package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "ownedProjects", ignore = true)
    User fromRequest(UserRequest request);

    UserRequest toRequest(User user);
}
