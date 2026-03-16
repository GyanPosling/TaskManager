package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.exception.EmailAlreadyExistsException;
import com.bsuir.taskmanager.exception.UserNotFoundException;
import com.bsuir.taskmanager.mapper.UserMapper;
import com.bsuir.taskmanager.model.dto.request.UserRequest;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.UserRepository;
import java.util.List;
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
    void findAllShouldMapAllUsers() {
        User firstUser = user(1L, "john", "john@example.com");
        User secondUser = user(2L, "kate", "kate@example.com");
        UserResponse firstResponse = new UserResponse(1L, "john", "john@example.com");
        UserResponse secondResponse = new UserResponse(2L, "kate", "kate@example.com");

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(userMapper.toResponse(firstUser)).thenReturn(firstResponse);
        when(userMapper.toResponse(secondUser)).thenReturn(secondResponse);

        List<UserResponse> result = userService.findAll();

        assertSame(firstResponse, result.get(0));
        assertSame(secondResponse, result.get(1));
    }

    @Test
    void findByIdShouldReturnMappedUser() {
        User user = user(1L, "john", "john@example.com");
        UserResponse response = new UserResponse(1L, "john", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.findById(1L);

        assertSame(response, result);
    }

    @Test
    void findByIdShouldThrowWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
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
    void updateShouldSaveUserWhenEmailIsUniqueForAnotherUser() {
        UserRequest request = new UserRequest("john-updated", "john.new@example.com");
        User user = user(1L, "john", "john@example.com");
        UserResponse response = new UserResponse(1L, "john-updated", "john.new@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("john.new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.update(1L, request);

        assertSame(response, result);
    }

    @Test
    void updateShouldThrowWhenUserMissing() {
        UserRequest request = new UserRequest("john-updated", "john.new@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenEmailAlreadyExistsForAnotherUser() {
        UserRequest request = new UserRequest("john-updated", "john.new@example.com");
        User user = user(1L, "john", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("john.new@example.com", 1L)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.update(1L, request));
    }

    @Test
    void deleteShouldRemoveExistingUser() {
        when(userRepository.existsById(5L)).thenReturn(true);

        userService.delete(5L);

        verify(userRepository).deleteById(5L);
    }

    @Test
    void deleteShouldThrowWhenUserMissing() {
        when(userRepository.existsById(5L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.delete(5L));
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}
