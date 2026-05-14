package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.WorkType;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
}
