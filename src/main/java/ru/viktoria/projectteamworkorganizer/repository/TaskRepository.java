package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {
}
