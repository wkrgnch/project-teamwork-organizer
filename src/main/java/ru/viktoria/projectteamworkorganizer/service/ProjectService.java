package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectStage;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;

import java.util.List;
import java.util.Optional;


public interface ProjectService {
    List<Project> findAll();

    Project create(ProjectCreateDto projectCreateDto);

    Optional<Project> findById(Integer id);

    List<ProjectStage> findStagesByProjectId(Integer projectId);

    List<Sprint> findSprintsByProjectId(Integer projectId);
}
