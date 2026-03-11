package com.bsuir.taskmanager.cache;

import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record TaskSearchQueryKey(
        QueryType queryType,
        Long ownerId,
        TaskStatus status,
        String tagName,
        LocalDate dueDate,
        int pageNumber,
        int pageSize,
        Sort sort
) {
    public enum QueryType {
        PROJECT_OWNER_STATUS_JPQL,
        TAG_DUE_DATE_JPQL,
        TAG_DUE_DATE_NATIVE
    }

    public static TaskSearchQueryKey forProjectOwnerAndStatus(
            Long ownerId,
            TaskStatus status,
            Pageable pageable
    ) {
        return new TaskSearchQueryKey(
                QueryType.PROJECT_OWNER_STATUS_JPQL,
                ownerId,
                status,
                null,
                null,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }

    public static TaskSearchQueryKey forTagAndDueDateJpql(
            String tagName,
            LocalDate dueDate,
            Pageable pageable
    ) {
        return new TaskSearchQueryKey(
                QueryType.TAG_DUE_DATE_JPQL,
                null,
                null,
                tagName,
                dueDate,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }

    public static TaskSearchQueryKey forTagAndDueDateNative(
            String tagName,
            LocalDate dueDate,
            Pageable pageable
    ) {
        return new TaskSearchQueryKey(
                QueryType.TAG_DUE_DATE_NATIVE,
                null,
                null,
                tagName,
                dueDate,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }

    @Override
    public Sort sort() {
        return sort == null ? Sort.unsorted() : sort;
    }

    public TaskSearchQueryKey {
        Objects.requireNonNull(queryType, "queryType must not be null");
    }
}
