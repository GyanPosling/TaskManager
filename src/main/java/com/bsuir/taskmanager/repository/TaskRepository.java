package com.bsuir.taskmanager.repository;

import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllWithTags();

    @Query("select distinct t from Task t left join fetch t.comments")
    List<Task> findAllWithComments();
}
