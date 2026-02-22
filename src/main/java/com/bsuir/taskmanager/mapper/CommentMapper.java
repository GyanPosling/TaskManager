package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.CommentRequest;
import com.bsuir.taskmanager.model.dto.response.CommentResponse;
import com.bsuir.taskmanager.model.entity.Comment;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "task", source = "task")
    @Mapping(target = "author", source = "author")
    Comment fromRequest(CommentRequest request, Task task, User author);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    CommentRequest toRequest(Comment comment);
}
