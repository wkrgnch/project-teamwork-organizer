package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("""
            select t from Task t
            left join fetch t.stage
            left join fetch t.sprint
            left join fetch t.workType
            left join fetch t.assignee
            where t.project.id = :projectId
            order by t.createdAt desc
            """)
    List<Task> findTasksByProjectId(@Param("projectId") Integer projectId);
}