package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.model.dto.request.TagRequest;
import com.bsuir.taskmanager.model.dto.response.TagResponse;
import com.bsuir.taskmanager.model.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse toResponse(Tag tag);

    Tag fromRequest(TagRequest request);

    TagRequest toRequest(Tag tag);
}
