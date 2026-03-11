package com.bsuir.taskmanager.repository;

import com.bsuir.taskmanager.model.entity.Task;
import com.bsuir.taskmanager.model.entity.TaskStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = "tags")
    @Query("select t from Task t")
    List<Task> findAllWithTags();

    @Query("select distinct t from Task "
            + "t left join fetch t.comments")
    List<Task> findAllWithComments();

    @Query(
            value = "select t from Task t "
                    + "join t.project p "
                    + "join p.owner o "
                    + "where o.id = :ownerId and t.status = :status",
            countQuery = "select count(t) from Task t "
                    + "join t.project p "
                    + "join p.owner o "
                    + "where o.id = :ownerId and t.status = :status"
    )
    Page<Task> findByProjectOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") TaskStatus status,
            Pageable pageable
    );

    @Query(
            value = "select distinct t from Task t "
                    + "join t.tags tg "
                    + "where tg.name = :tagName and t.dueDate <= :dueDate",
            countQuery = "select count(distinct t) from Task t "
                    + "join t.tags tg "
                    + "where tg.name = :tagName and t.dueDate <= :dueDate"
    )
    Page<Task> findByTagNameAndDueDateBeforeEqualJpql(
            @Param("tagName") String tagName,
            @Param("dueDate") java.time.LocalDate dueDate,
            Pageable pageable
    );

    @Query(
            value = "select distinct t.* from tasks t "
                    + "join task_tags tt on tt.task_id = t.id "
                    + "join tags tg on tg.id = tt.tag_id "
                    + "where tg.name = :tagName and t.due_date <= :dueDate",
            countQuery = "select count(distinct t.id) from tasks t "
                    + "join task_tags tt on tt.task_id = t.id "
                    + "join tags tg on tg.id = tt.tag_id "
                    + "where tg.name = :tagName and t.due_date <= :dueDate",
            nativeQuery = true
    )
    Page<Task> findByTagNameAndDueDateBeforeEqualNative(
            @Param("tagName") String tagName,
            @Param("dueDate") java.time.LocalDate dueDate,
            Pageable pageable
    );
}
