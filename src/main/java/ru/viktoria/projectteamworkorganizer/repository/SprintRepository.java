package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectIdOrderByStartDateAsc(Integer projectId);
}
