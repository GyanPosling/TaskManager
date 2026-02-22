package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.model.dto.request.TagRequest;
import com.bsuir.taskmanager.model.dto.response.TagResponse;
import java.util.List;

public interface TagService {
    List<TagResponse> findAll();

    TagResponse findById(Long id);

    TagResponse create(TagRequest request);

    TagResponse update(Long id, TagRequest request);

    void delete(Long id);
}
