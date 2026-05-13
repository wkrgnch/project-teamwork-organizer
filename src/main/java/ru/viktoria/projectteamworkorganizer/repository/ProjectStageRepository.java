package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.ProjectStage;

public interface ProjectStageRepository extends JpaRepository<ProjectStage, Integer> {
}
