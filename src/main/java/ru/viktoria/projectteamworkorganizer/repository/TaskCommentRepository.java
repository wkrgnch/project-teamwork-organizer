package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.TaskComment;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
}
