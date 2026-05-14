package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectStage;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStageStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStatusType;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.repository.ProjectStageRepository;
import ru.viktoria.projectteamworkorganizer.repository.SprintRepository;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private ProjectStageRepository projectStageRepository;
    private SprintRepository sprintRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectStageRepository projectStageRepository,
                              SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.projectStageRepository = projectStageRepository;
        this.sprintRepository = sprintRepository;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project create(ProjectCreateDto projectCreateDto) {
        Project project = new Project();

        project.setName(projectCreateDto.getName());
        project.setDescription(projectCreateDto.getDescription());
        project.setPublicDescription(projectCreateDto.getPublicDescription());
        project.setMethodology(projectCreateDto.getMethodology());
        project.setStatus(ProjectStatusType.PLANNED);
        project.setStartDate(projectCreateDto.getStartDate());
        project.setEndDate(projectCreateDto.getEndDate());
        project.setPublicProject(Boolean.TRUE.equals(projectCreateDto.getPublicProject()));
        project.setCreatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        if (savedProject.getMethodology() == ProjectMethodologyType.SCRUM)
            createScrumTemplate(savedProject);
        else if (savedProject.getMethodology() == ProjectMethodologyType.KANBAN)
            createKanbanTemplate(savedProject);
        return savedProject;
    }

    private void createScrumTemplate(Project project) {
        createStage(project, "Бэклог", "Список задач, которые планируются к выполнению", 1);
        createStage(project, "Планирование спринта", "Отбор задач в ближайший спринт", 2);
        createStage(project, "Спринт в работе", "Выполнение задач текущего спринта", 3);
        createStage(project, "Проверка результата", "Проверка выполненных задач", 4);
        createStage(project, "Завершено", "Завершённые задачи и результаты", 5);

        createFirstSprint(project);
    }

    private void createKanbanTemplate(Project project) {
        createStage(project, "Новые", "Новые задачи, которые ещё не взяты в работу", 1);
        createStage(project, "В очереди", "Задачи, ожидающие выполнения", 2);
        createStage(project, "В работе", "Задачи, которые сейчас выполняются", 3);
        createStage(project, "На проверке", "Задачи, переданные на проверку", 4);
        createStage(project, "Завершено", "Выполненные задачи", 5);
    }

    private void createStage(Project project, String name,
                             String description, Integer orderNumber) {

        ProjectStage stage = new ProjectStage();

        stage.setProject(project);
        stage.setName(name);
        stage.setDescription(description);
        stage.setStartDate(project.getStartDate());
        stage.setEndDate(project.getEndDate());
        stage.setStatus(ProjectStageStatusType.PLANNED);
        stage.setOrderNumber(orderNumber);

        projectStageRepository.save(stage);
    }

    private void createFirstSprint(Project  project) {
        Sprint sprint = new Sprint();

        LocalDate sprintStartDate = project.getStartDate();
        LocalDate sprintEndDate = sprintStartDate.plusDays(13);

        if (project.getEndDate() != null && project.getEndDate().isBefore(sprintEndDate)) {
            sprintEndDate = project.getEndDate();
        }

        sprint.setProject(project);
        sprint.setName("Спринт 1");
        sprint.setStartDate(sprintStartDate);
        sprint.setEndDate(sprintEndDate);
        sprint.setGoal("Первый спринт проекта");

        sprintRepository.save(sprint);
    }
}
