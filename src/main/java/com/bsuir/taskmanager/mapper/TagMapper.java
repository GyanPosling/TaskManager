package com.bsuir.taskmanager.mapper;

import com.bsuir.taskmanager.dto.request.TagRequest;
import com.bsuir.taskmanager.dto.response.TagResponse;
import com.bsuir.taskmanager.model.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        return response;
    }

    public Tag fromRequest(TagRequest request) {
        if (request == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        return tag;
    }

    public TagRequest toRequest(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagRequest request = new TagRequest();
        request.setName(tag.getName());
        return request;
    }
}
