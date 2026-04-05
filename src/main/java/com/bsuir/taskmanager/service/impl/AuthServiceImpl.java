package com.bsuir.taskmanager.service.impl;

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
import com.bsuir.taskmanager.security.JwtService;
import com.bsuir.taskmanager.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        String token = jwtService.generateAccessToken(userDetails);
        return new AuthResponse(token, "Bearer", userMapper.toResponse(user));
    }
}
