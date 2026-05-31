package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.TaskResultDto;
import ru.viktoria.projectteamworkorganizer.entity.*;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RequestStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;
import ru.viktoria.projectteamworkorganizer.repository.*;
import ru.viktoria.projectteamworkorganizer.service.TaskService;
import ru.viktoria.projectteamworkorganizer.service.UserActionLogService;

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
    private final ProjectRequestRepository projectRequestRepository;
    private final UserActionLogService userActionLogService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           ProjectStageRepository projectStageRepository,
                           SprintRepository sprintRepository,
                           WorkTypeRepository workTypeRepository,
                           UserRepository userRepository,
                           ProjectMemberRepository projectMemberRepository,
                           ProjectRequestRepository projectRequestRepository,
                           UserActionLogService userActionLogService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectStageRepository = projectStageRepository;
        this.sprintRepository = sprintRepository;
        this.workTypeRepository = workTypeRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRequestRepository = projectRequestRepository;
        this.userActionLogService = userActionLogService;
    }

    @Override
    public List<Task> findTasksByProjectId(Integer projectId) {
        return taskRepository.findTasksByProjectId(projectId);
    }

    @Override
    public Optional<Task> findById(Integer taskId) {
        return taskRepository.findTaskDetailsById(taskId);
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

            if (Boolean.TRUE.equals(assignee.getDeleted())) {
                throw new IllegalStateException("Нельзя назначить задачу удалённому пользователю");
            }

            long executorRoleCount = projectMemberRepository.countMemberRole(
                    projectId,
                    assignee.getUsername(),
                    RoleType.WORK_EXECUTOR
            );

            if (executorRoleCount == 0) {
                throw new IllegalStateException("Исполнителем можно назначить только участника с ролью исполнителя");
            }
        }

        Optional<User> createdByOptional = userRepository.findByUsername(currentUsername);

        if (createdByOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        User createdByUser = createdByOptional.get();

        ProjectRequest request = null;

        if (taskCreateDto.getRequestId() != null) {
            request = projectRequestRepository.findDetailedById(taskCreateDto.getRequestId())
                    .orElseThrow(() -> new IllegalStateException("Заявка не найдена"));

            if (!request.getProject().getId().equals(projectId)) {
                throw new IllegalStateException("Заявка не относится к этому проекту");
            }

            if (request.getStatus() != RequestStatusType.ACCEPTED) {
                throw new IllegalStateException("Создать задачу можно только по принятой заявке");
            }
        }

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
        task.setRequest(request);
        task.setDeadline(taskCreateDto.getDeadline());
        task.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        if (request != null) {
            request.setStatus(RequestStatusType.CONVERTED_TO_TASK);
            request.setUpdatedAt(LocalDateTime.now());
            projectRequestRepository.save(request);

            userActionLogService.log(
                    currentUsername,
                    UserActionType.UPDATE_REQUEST,
                    ActionObjectType.REQUEST,
                    request.getId(),
                    request.getTitle(),
                    "По заявке \"" + request.getTitle() + "\" создана задача \"" + savedTask.getTitle() + "\""
            );
        }

        userActionLogService.log(
                currentUsername,
                UserActionType.CREATE_TASK,
                ActionObjectType.TASK,
                savedTask.getId(),
                savedTask.getTitle(),
                "Создана задача \"" + savedTask.getTitle()
                        + "\" в проекте \"" + project.getName() + "\""
        );

        return savedTask;
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
    @Transactional
    public Integer changeStatus(Integer taskId,
                                TaskStatusType newStatus,
                                String currentUsername) {
        Optional<Task> taskOptional = taskRepository.findTaskDetailsById(taskId);

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

        updateTaskStatus(task, newStatus);
        taskRepository.save(task);

        userActionLogService.log(
                currentUsername,
                UserActionType.CHANGE_TASK_STATUS,
                ActionObjectType.TASK,
                task.getId(),
                task.getTitle(),
                "Статус задачи изменён на " + newStatus
        );

        return projectId;
    }

    @Override
    @Transactional
    public Integer takeTask(Integer taskId, String currentUsername) {
        Task task = findTaskOrThrow(taskId);

        if (!canTakeTask(task, currentUsername)) {
            throw new IllegalStateException("Нет прав для взятия задачи в работу");
        }

        Optional<User> userOptional = userRepository.findByUsername(currentUsername);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        User currentUser = userOptional.get();

        task.setAssignee(currentUser);
        updateTaskStatus(task, TaskStatusType.IN_PROGRESS);
        taskRepository.save(task);

        userActionLogService.log(
                currentUsername,
                UserActionType.ASSIGN_EXECUTOR,
                ActionObjectType.TASK,
                task.getId(),
                task.getTitle(),
                "Пользователь взял задачу \"" + task.getTitle() + "\" в работу"
        );

        return task.getProject().getId();
    }

    @Override
    @Transactional
    public Integer submitResult(Integer taskId,
                                TaskResultDto taskResultDto,
                                String currentUsername) {
        Task task = findTaskOrThrow(taskId);

        if (!canSubmitResult(task, currentUsername)) {
            throw new IllegalStateException("Нет прав для отправки результата по этой задаче");
        }

        task.setResultDescription(taskResultDto.getResultDescription().trim());

        if (taskResultDto.getResultUrl() != null && !taskResultDto.getResultUrl().isBlank()) {
            task.setResultUrl(taskResultDto.getResultUrl().trim());
        } else {
            task.setResultUrl(null);
        }

        updateTaskStatus(task, TaskStatusType.ON_REVIEW);
        taskRepository.save(task);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_TASK,
                ActionObjectType.TASK,
                task.getId(),
                task.getTitle(),
                "По задаче \"" + task.getTitle() + "\" отправлен результат на проверку"
        );

        return task.getProject().getId();
    }

    @Override
    @Transactional
    public Integer reviewTask(Integer taskId,
                              TaskStatusType newStatus,
                              String currentUsername) {
        Task task = findTaskOrThrow(taskId);

        if (!canReviewTask(task, currentUsername)) {
            throw new IllegalStateException("Нет прав для проверки этой задачи");
        }

        if (newStatus != TaskStatusType.DONE && newStatus != TaskStatusType.NEEDS_REVISION) {
            throw new IllegalStateException("Контрольный администратор может принять задачу или вернуть её на доработку");
        }

        updateTaskStatus(task, newStatus);
        taskRepository.save(task);

        userActionLogService.log(
                currentUsername,
                UserActionType.CHANGE_TASK_STATUS,
                ActionObjectType.TASK,
                task.getId(),
                task.getTitle(),
                "Контрольный администратор изменил статус задачи на " + newStatus
        );

        return task.getProject().getId();
    }

    @Override
    public boolean canTakeTask(Task task, String currentUsername) {
        Integer projectId = task.getProject().getId();

        boolean workExecutor = hasProjectRole(projectId, currentUsername, RoleType.WORK_EXECUTOR);
        boolean taskHasNoExecutor = task.getAssignee() == null;
        boolean taskIsAvailable = task.getStatus() == TaskStatusType.TO_DO
                || task.getStatus() == TaskStatusType.NEEDS_REVISION;

        return workExecutor && taskHasNoExecutor && taskIsAvailable;
    }

    @Override
    public boolean canSubmitResult(Task task, String currentUsername) {
        Integer projectId = task.getProject().getId();

        boolean workExecutor = hasProjectRole(projectId, currentUsername, RoleType.WORK_EXECUTOR);
        boolean assignedToCurrentUser = task.getAssignee() != null
                && task.getAssignee().getUsername().equals(currentUsername);
        boolean statusAllowsResult = task.getStatus() == TaskStatusType.IN_PROGRESS
                || task.getStatus() == TaskStatusType.NEEDS_REVISION;

        return workExecutor && assignedToCurrentUser && statusAllowsResult;
    }

    @Override
    public boolean canReviewTask(Task task, String currentUsername) {
        Integer projectId = task.getProject().getId();

        boolean controlAdmin = hasProjectRole(projectId, currentUsername, RoleType.CONTROL_ADMIN);
        boolean onReview = task.getStatus() == TaskStatusType.ON_REVIEW;

        return controlAdmin && onReview;
    }

    private Task findTaskOrThrow(Integer taskId) {
        Optional<Task> taskOptional = taskRepository.findTaskDetailsById(taskId);

        if (taskOptional.isEmpty()) {
            throw new IllegalStateException("Задача не найдена");
        }

        return taskOptional.get();
    }

    private void updateTaskStatus(Task task, TaskStatusType newStatus) {
        LocalDateTime now = LocalDateTime.now();

        task.setStatus(newStatus);
        task.setUpdatedAt(now);

        if (newStatus == TaskStatusType.DONE) {
            task.setCompletedAt(now);
        }

        if (newStatus == TaskStatusType.CLOSED) {
            task.setClosedAt(now);
        }
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
