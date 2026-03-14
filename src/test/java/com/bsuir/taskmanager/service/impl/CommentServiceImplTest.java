package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.exception.CommentNotFoundException;
import com.bsuir.taskmanager.mapper.CommentMapper;
import com.bsuir.taskmanager.model.dto.request.CommentRequest;
import com.bsuir.taskmanager.model.dto.response.CommentResponse;
import com.bsuir.taskmanager.model.entity.Comment;
import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.CommentRepository;
import com.bsuir.taskmanager.repository.TaskRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentMapper commentMapper;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(
                commentRepository,
                taskRepository,
                userRepository,
                commentMapper
        );
    }

    @Test
    void createShouldSaveCommentWithTaskAndAuthor() {
        CommentRequest request = new CommentRequest("Looks good", 6L, 2L);
        Task task = task(6L);
        User author = user(2L);
        Comment comment = comment(9L, "Looks good");
        CommentResponse response = new CommentResponse(
                9L,
                "Looks good",
                LocalDateTime.of(2026, 3, 14, 10, 0),
                6L,
                2L
        );

        when(taskRepository.findById(6L)).thenReturn(java.util.Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(author));
        when(commentMapper.fromRequest(request, task, author)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(response);

        CommentResponse result = commentService.create(request);

        assertSame(response, result);
        verify(commentRepository).save(comment);
    }

    @Test
    void updateShouldThrowWhenCommentMissing() {
        CommentRequest request = new CommentRequest("Updated", 6L, 2L);

        when(commentRepository.findById(12L)).thenReturn(java.util.Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.update(12L, request));
    }

    @Test
    void deleteShouldRemoveExistingComment() {
        when(commentRepository.existsById(15L)).thenReturn(true);

        commentService.delete(15L);

        verify(commentRepository).deleteById(15L);
    }

    private Task task(Long id) {
        Task task = new Task();
        task.setId(id);
        return task;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Comment comment(Long id, String text) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText(text);
        return comment;
    }
}
