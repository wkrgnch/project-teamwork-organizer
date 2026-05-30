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
import java.util.*;

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
            throw new IllegalStateException("Этап проекта не найден");
        }

        ProjectStage stage = stageOptional.get();

        if (!stage.getProject().getId().equals(projectId)) {
            throw new IllegalStateException("Выбранный этап не относится к этому проекту");
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

    @Override
    public Map<Integer, List<Task>> findTasksByStageForProject(Integer projectId) {
        List<Task> tasks = taskRepository.findTasksByProjectId(projectId);

        Map<Integer, List<Task>> tasksByStage = new HashMap<>();

        for (Task task : tasks) {
            if (task.getStage() != null) {
                Integer stageId = task.getStage().getId();

                if (!tasksByStage.containsKey(stageId)) {
                    tasksByStage.put(stageId, new ArrayList<>());
                }

                tasksByStage.get(stageId).add(task);
            }
        }

        return tasksByStage;
    }

    @Override
    @Transactional
    public Integer changeStage(Integer taskId, Integer stageId, String currentUsername) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isEmpty()) {
            throw new IllegalStateException("Задача не найдена");
        }

        Task task = taskOptional.get();
        Integer projectId = task.getProject().getId();

        boolean canManage = canCreateTask(projectId, currentUsername);

        if (!canManage) {
            throw new IllegalStateException("Нет прав для изменения стадии задачи");
        }

        Optional<ProjectStage> stageOptional = projectStageRepository.findById(stageId);

        if (stageOptional.isEmpty()) {
            throw new IllegalStateException("Этап проекта не найден");
        }

        ProjectStage newStage = stageOptional.get();

        if (!newStage.getProject().getId().equals(projectId)) {
            throw new IllegalStateException("Выбранный этап не относится к этому проекту");
        }

        task.setStage(newStage);
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);

        return projectId;
    }

    @Override
    @Transactional
    public Integer changeStatus(Integer taskId,
                                TaskStatusType newStatus,
                                String currentUsername) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isEmpty()) {
            throw new IllegalStateException("Задача не найдена");
        }

        Task task = taskOptional.get();
        Integer projectId = task.getProject().getId();

        boolean projectAdmin = hasProjectRole(projectId, currentUsername, RoleType.PROJECT_ORGANIZATION_ADMIN);
        boolean controlAdmin = hasProjectRole(projectId, currentUsername, RoleType.CONTROL_ADMIN);
        boolean workExecutor = hasProjectRole(projectId, currentUsername, RoleType.WORK_EXECUTOR);

        boolean allowed = false;

        if (projectAdmin) {
            allowed = true;
        }

        if (!allowed && controlAdmin) {
            if (newStatus == TaskStatusType.DONE
                    || newStatus == TaskStatusType.NEEDS_REVISION
                    || newStatus == TaskStatusType.CLOSED) {
                allowed = true;
            }
        }

        if (!allowed && workExecutor) {
            boolean assignedToCurrentUser = task.getAssignee() != null
                    && task.getAssignee().getUsername().equals(currentUsername);

            if (assignedToCurrentUser
                    && (newStatus == TaskStatusType.IN_PROGRESS
                    || newStatus == TaskStatusType.ON_REVIEW)) {
                allowed = true;
            }
        }

        if (!allowed) {
            throw new IllegalStateException("Нет прав для изменения статуса задачи");
        }

        LocalDateTime now = LocalDateTime.now();

        task.setStatus(newStatus);
        task.setUpdatedAt(now);

        if (newStatus == TaskStatusType.DONE) {
            task.setCompletedAt(now);
        }

        if (newStatus == TaskStatusType.CLOSED) {
            task.setClosedAt(now);
        }

        taskRepository.save(task);

        return projectId;
    }

    private boolean hasProjectRole(Integer projectId, String username, RoleType roleType) {
        long count = projectMemberRepository.countMemberRole(projectId, username, roleType);
        return count > 0;
    }

    @Override
    public Map<TaskStatusType, List<Task>> findTasksByBoardStatusForProject(Integer projectId) {
        List<Task> tasks = taskRepository.findTasksByProjectId(projectId);

        Map<TaskStatusType, List<Task>> tasksByStatus = new HashMap<>();

        for (Task task : tasks) {
            TaskStatusType boardStatus = getBoardStatus(task.getStatus());

            if (!tasksByStatus.containsKey(boardStatus)) {
                tasksByStatus.put(boardStatus, new ArrayList<>());
            }

            tasksByStatus.get(boardStatus).add(task);
        }

        return tasksByStatus;
    }

    private TaskStatusType getBoardStatus(TaskStatusType status) {
        if (status == TaskStatusType.NEEDS_CLARIFICATION) {
            return TaskStatusType.TO_DO;
        }

        if (status == TaskStatusType.CLOSED) {
            return TaskStatusType.DONE;
        }

        return status;
    }
}