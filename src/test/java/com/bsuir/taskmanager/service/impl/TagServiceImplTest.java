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

    private Tag tag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
