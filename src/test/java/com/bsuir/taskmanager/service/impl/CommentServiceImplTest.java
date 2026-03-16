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
import java.util.List;
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
    void findAllShouldMapAllComments() {
        Comment firstComment = comment(1L, "First");
        Comment secondComment = comment(2L, "Second");
        CommentResponse firstResponse = commentResponse(1L, "First");
        CommentResponse secondResponse = commentResponse(2L, "Second");

        when(commentRepository.findAll()).thenReturn(List.of(firstComment, secondComment));
        when(commentMapper.toResponse(firstComment)).thenReturn(firstResponse);
        when(commentMapper.toResponse(secondComment)).thenReturn(secondResponse);

        List<CommentResponse> result = commentService.findAll();

        assertSame(firstResponse, result.get(0));
        assertSame(secondResponse, result.get(1));
    }

    @Test
    void findByIdShouldReturnMappedComment() {
        Comment comment = comment(9L, "Looks good");
        CommentResponse response = commentResponse(9L, "Looks good");

        when(commentRepository.findById(9L)).thenReturn(java.util.Optional.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(response);

        CommentResponse result = commentService.findById(9L);

        assertSame(response, result);
    }

    @Test
    void findByIdShouldThrowWhenCommentMissing() {
        when(commentRepository.findById(13L)).thenReturn(java.util.Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.findById(13L));
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
    void createShouldThrowWhenTaskMissing() {
        CommentRequest request = new CommentRequest("Looks good", 6L, 2L);

        when(taskRepository.findById(6L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.create(request));
    }

    @Test
    void createShouldThrowWhenAuthorMissing() {
        CommentRequest request = new CommentRequest("Looks good", 6L, 2L);
        Task task = task(6L);

        when(taskRepository.findById(6L)).thenReturn(java.util.Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.create(request));
    }

    @Test
    void updateShouldSaveUpdatedComment() {
        CommentRequest request = new CommentRequest("Updated", 6L, 2L);
        Comment existingComment = comment(12L, "Old");
        Task task = task(6L);
        User author = user(2L);
        CommentResponse response = commentResponse(12L, "Updated");

        when(commentRepository.findById(12L)).thenReturn(java.util.Optional.of(existingComment));
        when(taskRepository.findById(6L)).thenReturn(java.util.Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(author));
        when(commentRepository.save(existingComment)).thenReturn(existingComment);
        when(commentMapper.toResponse(existingComment)).thenReturn(response);

        CommentResponse result = commentService.update(12L, request);

        assertSame(response, result);
        assertSame(task, existingComment.getTask());
        assertSame(author, existingComment.getAuthor());
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

    @Test
    void deleteShouldThrowWhenCommentMissing() {
        when(commentRepository.existsById(15L)).thenReturn(false);

        assertThrows(CommentNotFoundException.class, () -> commentService.delete(15L));
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

    private CommentResponse commentResponse(Long id, String text) {
        return new CommentResponse(
                id,
                text,
                LocalDateTime.of(2026, 3, 14, 10, 0),
                6L,
                2L
        );
    }
}
