package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.ProjectStage;

import java.util.List;

public interface ProjectStageRepository extends JpaRepository<ProjectStage, Integer> {

    List<ProjectStage> findByProjectIdOrderByOrderNumberAsc(Integer projectId);
}
