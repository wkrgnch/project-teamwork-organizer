package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.*;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskStatusType;
import ru.viktoria.projectteamworkorganizer.entity.id.ProjectMemberId;
import ru.viktoria.projectteamworkorganizer.repository.*;
import ru.viktoria.projectteamworkorganizer.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectStageRepository projectStageRepository;
    private final SprintRepository sprintRepository;
    private final WorkTypeRepository workTypeRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           ProjectStageRepository projectStageRepository,
                           SprintRepository sprintRepository,
                           WorkTypeRepository workTypeRepository,
                           UserRepository userRepository,
                           ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectStageRepository = projectStageRepository;
        this.sprintRepository = sprintRepository;
        this.workTypeRepository = workTypeRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public List<Task> findTasksByProjectId(Integer projectId) {
        return taskRepository.findTasksByProjectId(projectId);
    }

    @Override
    @Transactional
    public Task createTask(Integer projectId, TaskCreateDto taskCreateDto, String currentUsername) {
        boolean canManage = canCreateTask(projectId, currentUsername);

        if (!canManage) {
            throw new IllegalStateException("Нет прав для создания задачи в этом проекте");
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Проект не найден");
        }

        Project project = projectOptional.get();

        Optional<ProjectStage> stageOptional = projectStageRepository.findById(taskCreateDto.getStageId());

        if (stageOptional.isEmpty()) {
            throw new IllegalStateException("Стадия не найдена");
        }

        ProjectStage stage = stageOptional.get();

        if (!stage.getProject().getId().equals(projectId)) {
            throw new IllegalStateException("Выбранная стадия не относится к этому проекту");
        }

        Optional<WorkType> workTypeOptional = workTypeRepository.findById(taskCreateDto.getWorkTypeId());

        if (workTypeOptional.isEmpty()) {
            throw new IllegalStateException("Тип работы не найден");
        }

        WorkType workType = workTypeOptional.get();

        Sprint sprint = null;

        if (taskCreateDto.getSprintId() != null) {
            Optional<Sprint> sprintOptional = sprintRepository.findById(taskCreateDto.getSprintId());

            if (sprintOptional.isEmpty()) {
                throw new IllegalStateException("Спринт не найден");
            }

            sprint = sprintOptional.get();

            if (!sprint.getProject().getId().equals(projectId)) {
                throw new IllegalStateException("Выбранный спринт не относится к этому проекту");
            }
        }

        User assignee = null;

        if (taskCreateDto.getAssigneeId() != null) {
            Optional<User> assigneeOptional = userRepository.findById(taskCreateDto.getAssigneeId());

            if (assigneeOptional.isEmpty()) {
                throw new IllegalStateException("Исполнитель не найден");
            }

            assignee = assigneeOptional.get();

            ProjectMemberId assigneeMemberId = new ProjectMemberId(projectId, assignee.getId());

            if (!projectMemberRepository.existsById(assigneeMemberId)) {
                throw new IllegalStateException("Исполнитель не является участником этого проекта");
            }
        }

        Optional<User> createdByOptional = userRepository.findByUsername(currentUsername);

        if (createdByOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        User createdByUser = createdByOptional.get();

        Task task = new Task();

        task.setProject(project);
        task.setStage(stage);
        task.setSprint(sprint);
        task.setTitle(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getDescription());
        task.setWorkType(workType);
        task.setPriority(taskCreateDto.getPriority());
        task.setStatus(TaskStatusType.TO_DO);
        task.setAssignee(assignee);
        task.setCreatedByUser(createdByUser);
        task.setDeadline(taskCreateDto.getDeadline());
        task.setCreatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    private boolean canCreateTask(Integer projectId, String username) {
        long count = projectMemberRepository.countMemberRole(
                projectId,
                username,
                RoleType.PROJECT_ORGANIZATION_ADMIN
        );

        return count > 0;
    }
}