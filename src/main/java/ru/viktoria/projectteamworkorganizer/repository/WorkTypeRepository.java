package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.WorkType;

public interface WorkTypeRepository extends JpaRepository<WorkType, Integer> {
}
