package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.CommentRequest;
import com.bsuir.taskmanager.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    List<CommentResponse> findAll();

    CommentResponse findById(Long id);

    CommentResponse create(CommentRequest request);

    CommentResponse update(Long id, CommentRequest request);

    void delete(Long id);
}
