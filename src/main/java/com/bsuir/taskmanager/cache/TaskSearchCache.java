package com.bsuir.taskmanager.cache;

import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

@Component
public class TaskSearchCache {
    private final Map<TaskSearchQueryKey, Page<TaskResponse>> cache = new HashMap<>();

    public synchronized Optional<Page<TaskResponse>> get(TaskSearchQueryKey key) {
        return Optional.ofNullable(cache.get(key));
    }

    public synchronized void put(TaskSearchQueryKey key, Page<TaskResponse> value) {
        cache.put(key, copyPage(value));
    }

    public synchronized void clear() {
        cache.clear();
    }

    private Page<TaskResponse> copyPage(Page<TaskResponse> source) {
        return new PageImpl<>(
                List.copyOf(source.getContent()),
                source.getPageable(),
                source.getTotalElements()
        );
    }
}
