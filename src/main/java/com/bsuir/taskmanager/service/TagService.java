package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.TagRequest;
import com.bsuir.taskmanager.dto.response.TagResponse;
import com.bsuir.taskmanager.mapper.TagMapper;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponse> findAll() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    public TagResponse findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + id));
        return tagMapper.toResponse(tag);
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        Tag tag = tagMapper.fromRequest(request);
        Tag saved = tagRepository.save(tag);
        return tagMapper.toResponse(saved);
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + id));
        tag.setName(request.getName());
        Tag saved = tagRepository.save(tag);
        return tagMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new EntityNotFoundException("Tag not found: " + id);
        }
        tagRepository.deleteById(id);
    }
}
