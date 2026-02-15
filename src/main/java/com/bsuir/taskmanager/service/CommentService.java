package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.CommentRequest;
import com.bsuir.taskmanager.dto.response.CommentResponse;
import com.bsuir.taskmanager.mapper.CommentMapper;
import com.bsuir.taskmanager.model.entity.Comment;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.CommentRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public List<CommentResponse> findAll() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    public CommentResponse findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        return commentMapper.toResponse(comment);
    }

    @Transactional
    public CommentResponse create(CommentRequest request) {
        Task task = getTask(request.getTaskId());
        User author = getAuthor(request.getAuthorId());
        Comment comment = commentMapper.fromRequest(request, task, author);
        Comment saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    @Transactional
    public CommentResponse update(Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + id));
        Task task = getTask(request.getTaskId());
        User author = getAuthor(request.getAuthorId());
        comment.setText(request.getText());
        comment.setTask(task);
        comment.setAuthor(author);
        Comment saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment not found: " + id);
        }
        commentRepository.deleteById(id);
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
    }

    private User getAuthor(Long authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorId));
    }
}
