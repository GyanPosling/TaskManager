package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.exception.TagNotFoundException;
import com.bsuir.taskmanager.mapper.TagMapper;
import com.bsuir.taskmanager.model.dto.request.TagRequest;
import com.bsuir.taskmanager.model.dto.response.TagResponse;
import com.bsuir.taskmanager.model.entity.Tag;
import com.bsuir.taskmanager.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private TaskSearchCache taskSearchCache;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository, tagMapper, taskSearchCache);
    }

    @Test
    void findAllShouldMapAllTags() {
        Tag firstTag = tag(1L, "backend");
        Tag secondTag = tag(2L, "frontend");
        TagResponse firstResponse = new TagResponse(1L, "backend");
        TagResponse secondResponse = new TagResponse(2L, "frontend");

        when(tagRepository.findAll()).thenReturn(List.of(firstTag, secondTag));
        when(tagMapper.toResponse(firstTag)).thenReturn(firstResponse);
        when(tagMapper.toResponse(secondTag)).thenReturn(secondResponse);

        List<TagResponse> result = tagService.findAll();

        assertSame(firstResponse, result.get(0));
        assertSame(secondResponse, result.get(1));
    }

    @Test
    void findByIdShouldReturnMappedTag() {
        Tag tag = tag(8L, "urgent");
        TagResponse response = new TagResponse(8L, "urgent");

        when(tagRepository.findById(8L)).thenReturn(java.util.Optional.of(tag));
        when(tagMapper.toResponse(tag)).thenReturn(response);

        TagResponse result = tagService.findById(8L);

        assertSame(response, result);
    }

    @Test
    void findByIdShouldThrowWhenTagMissing() {
        when(tagRepository.findById(8L)).thenReturn(java.util.Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.findById(8L));
    }

    @Test
    void createShouldSaveTagAndClearCache() {
        TagRequest request = new TagRequest("backend");
        Tag tag = tag(3L, "backend");
        TagResponse response = new TagResponse(3L, "backend");

        when(tagMapper.fromRequest(request)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toResponse(tag)).thenReturn(response);

        TagResponse result = tagService.create(request);

        assertSame(response, result);
        verify(taskSearchCache).clear();
    }

    @Test
    void updateShouldSaveTagAndClearCache() {
        TagRequest request = new TagRequest("urgent");
        Tag tag = tag(8L, "old-name");
        TagResponse response = new TagResponse(8L, "urgent");

        when(tagRepository.findById(8L)).thenReturn(java.util.Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toResponse(tag)).thenReturn(response);

        TagResponse result = tagService.update(8L, request);

        assertSame(response, result);
        verify(taskSearchCache).clear();
    }

    @Test
    void updateShouldThrowWhenTagMissing() {
        TagRequest request = new TagRequest("urgent");

        when(tagRepository.findById(8L)).thenReturn(java.util.Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.update(8L, request));
    }

    @Test
    void deleteShouldRemoveTagAndClearCache() {
        when(tagRepository.existsById(4L)).thenReturn(true);

        tagService.delete(4L);

        verify(tagRepository).deleteById(4L);
        verify(taskSearchCache).clear();
    }

    @Test
    void deleteShouldThrowWhenTagMissing() {
        when(tagRepository.existsById(4L)).thenReturn(false);

        assertThrows(TagNotFoundException.class, () -> tagService.delete(4L));
    }

    private Tag tag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
