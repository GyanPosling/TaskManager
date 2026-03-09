package com.bsuir.taskmanager.cache;

import com.bsuir.taskmanager.model.dto.response.TaskResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskSearchCache {
    private final Map<CacheKey, Page<TaskResponse>> cache = new HashMap<>();

    public synchronized Page<TaskResponse> get(CacheKey key) {
        return cache.get(key);
    }

    public synchronized void put(CacheKey key, Page<TaskResponse> value) {
        cache.put(key, value);
    }

    public synchronized void clear() {
        cache.clear();
    }
}
