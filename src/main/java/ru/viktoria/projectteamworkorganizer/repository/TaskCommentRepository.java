package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.TaskComment;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {

    @Query("""
            select c from TaskComment c
            left join fetch c.author
            where c.task.id = :taskId
            order by c.createdAt asc
            """)
    List<TaskComment> findCommentsByTaskId(@Param("taskId") Integer taskId);
}
