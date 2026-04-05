package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.exception.EmailAlreadyExistsException;
import com.bsuir.taskmanager.exception.InvalidCredentialsException;
import com.bsuir.taskmanager.exception.UsernameAlreadyExistsException;
import com.bsuir.taskmanager.mapper.UserMapper;
import com.bsuir.taskmanager.model.dto.request.AuthLoginRequest;
import com.bsuir.taskmanager.model.dto.request.AuthRegisterRequest;
import com.bsuir.taskmanager.model.dto.response.AuthResponse;
import com.bsuir.taskmanager.model.dto.response.UserResponse;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.UserRepository;
import com.bsuir.taskmanager.security.AuthUserDetails;
import com.bsuir.taskmanager.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                userMapper,
                jwtService,
                passwordEncoder,
                authenticationManager
        );
    }

    @Test
    void registerShouldCreateUserWhenUsernameAndEmailAreUnique() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                "john",
                "john@example.com",
                "MySecurePass123"
        );
        User savedUser = user(1L, "john", "john@example.com", "encoded-password");
        UserResponse response = new UserResponse(1L, "john", "john@example.com");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("MySecurePass123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = authService.register(request);

        assertSame(response, result);
        verify(passwordEncoder).encode("MySecurePass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenUsernameAlreadyExists() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                "john",
                "john@example.com",
                "MySecurePass123"
        );

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                "john",
                "john@example.com",
                "MySecurePass123"
        );

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        AuthLoginRequest request = new AuthLoginRequest("john", "MySecurePass123");
        User user = user(1L, "john", "john@example.com", "encoded-password");
        UserDetails userDetails = new AuthUserDetails(user);
        UserResponse responseUser = new UserResponse(1L, "john", "john@example.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(userDetails)).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(responseUser);

        AuthResponse result = authService.login(request);

        assertSame(responseUser, result.getUser());
        assertSame("jwt-token", result.getAccessToken());
        assertSame("Bearer", result.getTokenType());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(eq(userDetails));
    }

    @Test
    void loginShouldThrowWhenAuthenticationFails() {
        AuthLoginRequest request = new AuthLoginRequest("john", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(userRepository, never()).findByUsername(any());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void loginShouldThrowWhenAuthenticatedPrincipalUserMissingInDatabase() {
        AuthLoginRequest request = new AuthLoginRequest("john", "MySecurePass123");
        User user = user(1L, "john", "john@example.com", "encoded-password");
        UserDetails userDetails = new AuthUserDetails(user);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateAccessToken(any());
    }

    private User user(Long id, String username, String email, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
