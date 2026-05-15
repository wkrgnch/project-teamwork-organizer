package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskPriorityType;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.TaskService;
import ru.viktoria.projectteamworkorganizer.service.WorkTypeService;

import java.security.Principal;
import java.util.Optional;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final WorkTypeService workTypeService;

    public TaskController(TaskService taskService,
                          ProjectService projectService,
                          WorkTypeService workTypeService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.workTypeService = workTypeService;
    }

    @GetMapping("/projects/{projectId}/tasks/new")
    public String showCreateTaskForm(@PathVariable Integer projectId,
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

        model.addAttribute("project", project);
        model.addAttribute("taskForm", new TaskCreateDto());
        model.addAttribute("stages", projectService.findStagesByProjectId(projectId));
        model.addAttribute("sprints", projectService.findSprintsByProjectId(projectId));
        model.addAttribute("members", projectService.findMembersByProjectId(projectId));
        model.addAttribute("workTypes", workTypeService.findAll());
        model.addAttribute("priorities", TaskPriorityType.values());

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
            model.addAttribute("project", project);
            model.addAttribute("stages", projectService.findStagesByProjectId(projectId));
            model.addAttribute("sprints", projectService.findSprintsByProjectId(projectId));
            model.addAttribute("members", projectService.findMembersByProjectId(projectId));
            model.addAttribute("workTypes", workTypeService.findAll());
            model.addAttribute("priorities", TaskPriorityType.values());
            return "task-form";
        }

        try {
            taskService.createTask(projectId, taskForm, principal.getName());
        } catch (IllegalStateException exception) {
            model.addAttribute("project", project);
            model.addAttribute("stages", projectService.findStagesByProjectId(projectId));
            model.addAttribute("sprints", projectService.findSprintsByProjectId(projectId));
            model.addAttribute("members", projectService.findMembersByProjectId(projectId));
            model.addAttribute("workTypes", workTypeService.findAll());
            model.addAttribute("priorities", TaskPriorityType.values());
            model.addAttribute("errorMessage", exception.getMessage());
            return "task-form";
        }

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/tasks/{taskId}/stage")
    public String changeTaskStage(@PathVariable Integer taskId,
                                  @RequestParam("stageId") Integer stageId,
                                  Principal principal) {
        Integer projectId;

        try {
            projectId = taskService.changeStage(taskId, stageId, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + projectId + "/board";
    }
}