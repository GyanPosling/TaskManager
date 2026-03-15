package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.exception.EmailAlreadyExistsException;
import com.bsuir.taskmanager.mapper.UserMapper;
import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void createShouldSaveUserWhenEmailIsUnique() {
        UserRequest request = new UserRequest("john", "john@example.com");
        User user = user(1L, "john", "john@example.com");
        UserResponse response = new UserResponse(1L, "john", "john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.fromRequest(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertSame(response, result);
        verify(userRepository).save(user);
    }

    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        UserRequest request = new UserRequest("john", "john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteShouldRemoveExistingUser() {
        when(userRepository.existsById(5L)).thenReturn(true);

        userService.delete(5L);

        verify(userRepository).deleteById(5L);
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}
