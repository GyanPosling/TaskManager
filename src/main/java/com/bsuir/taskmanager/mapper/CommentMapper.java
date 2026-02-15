package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.dto.request.CommentRequest;
import com.bsuir.taskmanager.dto.response.CommentResponse;
import com.bsuir.taskmanager.model.entity.Comment;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setCreatedAt(comment.getCreatedAt());
        response.setTaskId(comment.getTask() != null ? comment.getTask().getId() : null);
        response.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        return response;
    }

    public Comment fromRequest(CommentRequest request, Task task, User author) {
        if (request == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setTask(task);
        comment.setAuthor(author);
        return comment;
    }

    public CommentRequest toRequest(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentRequest request = new CommentRequest();
        request.setText(comment.getText());
        request.setTaskId(comment.getTask() != null ? comment.getTask().getId() : null);
        request.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        return request;
    }
}
