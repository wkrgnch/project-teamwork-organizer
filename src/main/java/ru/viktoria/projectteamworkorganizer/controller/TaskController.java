package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.TaskCommentCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.TaskResultDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;
import ru.viktoria.projectteamworkorganizer.entity.Task;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskCommentType;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskPriorityType;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskStatusType;
import ru.viktoria.projectteamworkorganizer.service.ProjectRequestService;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.TaskCommentService;
import ru.viktoria.projectteamworkorganizer.service.TaskService;
import ru.viktoria.projectteamworkorganizer.service.WorkTypeService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final WorkTypeService workTypeService;
    private final TaskCommentService taskCommentService;
    private final ProjectRequestService projectRequestService;

    public TaskController(TaskService taskService,
                          ProjectService projectService,
                          WorkTypeService workTypeService,
                          TaskCommentService taskCommentService,
                          ProjectRequestService projectRequestService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.workTypeService = workTypeService;
        this.taskCommentService = taskCommentService;
        this.projectRequestService = projectRequestService;
    }

    @GetMapping("/projects/{projectId}/tasks/new")
    public String showCreateTaskForm(@PathVariable Integer projectId,
                                     @RequestParam(value = "requestId", required = false) Integer requestId,
                                     Model model,
                                     Principal principal) {
        Optional<Project> projectOptional = projectService.findById(projectId);

        if (projectOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден");
        }

        Project project = projectOptional.get();

        if (!projectService.canManageProject(projectId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        TaskCreateDto taskForm = new TaskCreateDto();

        if (requestId != null) {
            ProjectRequest request = projectRequestService.findVisibleRequest(requestId, principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));

            if (!request.getProject().getId().equals(projectId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заявка не относится к этому проекту");
            }

            taskForm.setRequestId(request.getId());
            taskForm.setTitle(request.getTitle());
            taskForm.setDescription(buildDescriptionFromRequest(request));
            taskForm.setDeadline(request.getDesiredDeadline());
        }

        fillTaskFormModel(model, project, taskForm);

        return "task-form";
    }

    @PostMapping("/projects/{projectId}/tasks")
    public String createTask(@PathVariable Integer projectId,
                             @Valid @ModelAttribute("taskForm") TaskCreateDto taskForm,
                             BindingResult bindingResult,
                             Model model,
                             Principal principal) {
        Optional<Project> projectOptional = projectService.findById(projectId);

        if (projectOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден");
        }

        Project project = projectOptional.get();

        if (!projectService.canManageProject(projectId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        if (bindingResult.hasErrors()) {
            fillTaskFormModel(model, project, taskForm);
            return "task-form";
        }

        try {
            taskService.createTask(projectId, taskForm, principal.getName());
        } catch (IllegalStateException exception) {
            fillTaskFormModel(model, project, taskForm);
            model.addAttribute("errorMessage", exception.getMessage());
            return "task-form";
        }

        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/tasks/{taskId}")
    public String showTaskDetails(@PathVariable Integer taskId,
                                  Model model,
                                  Principal principal) {
        Task task = getTaskForCurrentUser(taskId, principal.getName());

        fillTaskDetailsModel(model, task, new TaskCommentCreateDto(), new TaskResultDto(), principal.getName());

        return "task-details";
    }

    @PostMapping("/tasks/{taskId}/take")
    public String takeTask(@PathVariable Integer taskId,
                           Principal principal) {
        try {
            taskService.takeTask(taskId, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/tasks/" + taskId;
    }

    @PostMapping("/tasks/{taskId}/submit-result")
    public String submitResult(@PathVariable Integer taskId,
                               @Valid @ModelAttribute("resultForm") TaskResultDto resultForm,
                               BindingResult bindingResult,
                               Model model,
                               Principal principal) {
        Task task = getTaskForCurrentUser(taskId, principal.getName());

        if (bindingResult.hasErrors()) {
            fillTaskDetailsModel(model, task, new TaskCommentCreateDto(), resultForm, principal.getName());
            return "task-details";
        }

        try {
            taskService.submitResult(taskId, resultForm, principal.getName());
        } catch (IllegalStateException exception) {
            fillTaskDetailsModel(model, task, new TaskCommentCreateDto(), resultForm, principal.getName());
            model.addAttribute("errorMessage", exception.getMessage());
            return "task-details";
        }

        return "redirect:/tasks/" + taskId;
    }

    @PostMapping("/tasks/{taskId}/review")
    public String reviewTask(@PathVariable Integer taskId,
                             @RequestParam("status") TaskStatusType status,
                             Principal principal) {
        try {
            taskService.reviewTask(taskId, status, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/tasks/" + taskId;
    }

    @PostMapping("/tasks/{taskId}/comments")
    public String addComment(@PathVariable Integer taskId,
                             @Valid @ModelAttribute("commentForm") TaskCommentCreateDto commentForm,
                             BindingResult bindingResult,
                             Model model,
                             Principal principal) {
        Task task = getTaskForCurrentUser(taskId, principal.getName());

        if (bindingResult.hasErrors()) {
            fillTaskDetailsModel(model, task, commentForm, new TaskResultDto(), principal.getName());
            return "task-details";
        }

        try {
            taskCommentService.addComment(taskId, commentForm, principal.getName());
        } catch (IllegalStateException exception) {
            fillTaskDetailsModel(model, task, commentForm, new TaskResultDto(), principal.getName());
            model.addAttribute("errorMessage", exception.getMessage());
            return "task-details";
        }

        return "redirect:/tasks/" + taskId;
    }

    @PostMapping("/tasks/{taskId}/status")
    public String changeTaskStatus(@PathVariable Integer taskId,
                                   @RequestParam("status") TaskStatusType status,
                                   Principal principal) {
        Integer projectId;

        try {
            projectId = taskService.changeStatus(taskId, status, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + projectId + "/board";
    }

    private void fillTaskFormModel(Model model, Project project, TaskCreateDto taskForm) {
        model.addAttribute("project", project);
        model.addAttribute("taskForm", taskForm);
        model.addAttribute("stages", projectService.findStagesByProjectId(project.getId()));
        model.addAttribute("sprints", projectService.findSprintsByProjectId(project.getId()));
        model.addAttribute("executors", projectService.findExecutorsByProjectId(project.getId()));
        model.addAttribute("workTypes", workTypeService.findAll());
        model.addAttribute("priorities", TaskPriorityType.values());
    }

    private String buildDescriptionFromRequest(ProjectRequest request) {
        StringBuilder description = new StringBuilder();

        description.append(request.getDescription());

        if (request.getGoal() != null && !request.getGoal().isBlank()) {
            description.append("\n\nЦель заявки: ").append(request.getGoal());
        }

        description.append("\n\nОжидаемый результат: ").append(request.getExpectedResult());

        if (request.getMaterialUrl() != null && !request.getMaterialUrl().isBlank()) {
            description.append("\n\nМатериалы: ").append(request.getMaterialUrl());
        }

        return description.toString();
    }

    private Task getTaskForCurrentUser(Integer taskId, String currentUsername) {
        Optional<Task> taskOptional = taskService.findById(taskId);

        if (taskOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена");
        }

        Task task = taskOptional.get();
        Integer projectId = task.getProject().getId();

        if (!projectService.isProjectMember(projectId, currentUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        return task;
    }

    private void fillTaskDetailsModel(Model model,
                                      Task task,
                                      TaskCommentCreateDto commentForm,
                                      TaskResultDto resultForm,
                                      String currentUsername) {
        model.addAttribute("task", task);
        model.addAttribute("comments", taskCommentService.findCommentsByTaskId(task.getId()));
        model.addAttribute("commentForm", commentForm);
        model.addAttribute("resultForm", resultForm);
        model.addAttribute("commentTypes", TaskCommentType.values());
        model.addAttribute("taskStatusLabels", getTaskStatusLabels());
        model.addAttribute("commentTypeLabels", getCommentTypeLabels());
        model.addAttribute("canTakeTask", taskService.canTakeTask(task, currentUsername));
        model.addAttribute("canSubmitResult", taskService.canSubmitResult(task, currentUsername));
        model.addAttribute("canReviewTask", taskService.canReviewTask(task, currentUsername));
    }

    private Map<TaskStatusType, String> getTaskStatusLabels() {
        Map<TaskStatusType, String> labels = new HashMap<>();

        labels.put(TaskStatusType.TO_DO, "Новые");
        labels.put(TaskStatusType.IN_PROGRESS, "В работе");
        labels.put(TaskStatusType.NEEDS_CLARIFICATION, "Требует уточнения");
        labels.put(TaskStatusType.ON_REVIEW, "На проверке");
        labels.put(TaskStatusType.NEEDS_REVISION, "На доработке");
        labels.put(TaskStatusType.DONE, "Завершено");
        labels.put(TaskStatusType.CLOSED, "Закрыто");

        return labels;
    }

    private Map<TaskCommentType, String> getCommentTypeLabels() {
        Map<TaskCommentType, String> labels = new HashMap<>();

        labels.put(TaskCommentType.COMMON, "Обычный комментарий");
        labels.put(TaskCommentType.PROGRESS, "Ход выполнения");
        labels.put(TaskCommentType.CONTROL, "Контроль");
        labels.put(TaskCommentType.PROBLEM, "Проблема");
        labels.put(TaskCommentType.CLARIFICATION_REQUEST, "Запрос уточнения");
        labels.put(TaskCommentType.REVISION_COMMENT, "Комментарий к доработке");

        return labels;
    }
}
