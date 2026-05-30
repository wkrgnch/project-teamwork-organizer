package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.SprintCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.repository.SprintRepository;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.SprintService;

import java.util.Optional;

@Service
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    public SprintServiceImpl(SprintRepository sprintRepository,
                             ProjectRepository projectRepository,
                             ProjectService projectService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @Override
    @Transactional
    public Sprint createSprint(Integer projectId,
                               SprintCreateDto sprintCreateDto,
                               String currentUsername) {
        boolean canManage = projectService.canManageProject(projectId, currentUsername);

        if (!canManage) {
            throw new IllegalStateException("Нет прав для создания спринта");
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Проект не найден");
        }

        Project project = projectOptional.get();

        if (project.getMethodology() != ProjectMethodologyType.SCRUM) {
            throw new IllegalStateException("Спринты можно создавать только для Scrum-проектов");
        }

        if (sprintCreateDto.getEndDate().isBefore(sprintCreateDto.getStartDate())) {
            throw new IllegalStateException("Дата окончания спринта не может быть раньше даты начала");
        }

        Sprint sprint = new Sprint();

        sprint.setProject(project);
        sprint.setName(sprintCreateDto.getName());
        sprint.setStartDate(sprintCreateDto.getStartDate());
        sprint.setEndDate(sprintCreateDto.getEndDate());
        sprint.setGoal(sprintCreateDto.getGoal());

        return sprintRepository.save(sprint);
    }
}