package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectMemberAddDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskStatusType;
import ru.viktoria.projectteamworkorganizer.service.ProjectRequestService;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.TaskService;

import java.security.Principal;
import java.util.*;

@Controller
public class ProjectController {
    private ProjectService projectService;
    private final TaskService taskService;
    private ProjectRequestService projectRequestService;

    public ProjectController(ProjectService projectService,
                             TaskService taskService,
                             ProjectRequestService projectRequestService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.projectRequestService = projectRequestService;
    }

    @GetMapping("/projects")
    public String showProjects(Model model, Principal principal) {
        model.addAttribute("projects", projectService.findProjectsForUser(principal.getName()));
        return "projects";
    }

    @GetMapping("/projects/new")
    public String showCreateProjectForm(Model model){
        model.addAttribute("projectForm", new ProjectCreateDto());
        model.addAttribute("methodologies", ProjectMethodologyType.values());
        return "project-form";
    }

    @PostMapping("/projects")
    public String createProject(@Valid @ModelAttribute("projectForm") ProjectCreateDto projectForm,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("methodologies", ProjectMethodologyType.values());
            return "project-form";
        }

        projectService.create(projectForm, principal.getName());
        return "redirect:/projects";
    }

    @GetMapping("projects/{id}")
    public String showProjectDetails(@PathVariable Integer id, Model model, Principal principal) {
        Optional<Project> projectOptional = projectService.findById(id);

        if (projectOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден" );

        Project project = projectOptional.get();

        if (!projectService.isProjectMember(id, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        model.addAttribute("project", project);
        model.addAttribute("stages", projectService.findStagesByProjectId(id));
        model.addAttribute("projectStatuses", ProjectStatusType.values());
        model.addAttribute("sprints", projectService.findSprintsByProjectId(id));
        model.addAttribute("members", projectService.findMembersByProjectId(id));
        model.addAttribute("tasks", taskService.findTasksByProjectId(id));
        model.addAttribute("taskStatuses", TaskStatusType.values());
        model.addAttribute("canManageProject", projectService.canManageProject(id, principal.getName()));
        model.addAttribute("currentUsername", principal.getName());
        model.addAttribute("canCreateRequest", projectRequestService.canCreateRequest(id, principal.getName()));
        model.addAttribute("canManageRequests", projectRequestService.canManageRequests(id, principal.getName()));
        return "project-details";
    }

    @PostMapping("/projects/{id}/status")
    public String changeProjectStatus(@PathVariable Integer id,
                                      @RequestParam("status") ProjectStatusType status,
                                      Principal principal) {
        try {
            projectService.changeProjectStatus(id, status, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + id;
    }

    @GetMapping("/projects/{id}/members/new")
    public String showAddMemberForm(@PathVariable Integer id,
                                    Model model,
                                    Principal principal) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        if (!projectService.canManageProject(id, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        model.addAttribute("project", project);
        model.addAttribute("memberForm", new ProjectMemberAddDto());
        model.addAttribute("roles", getProjectRoles());

        return "project-member-form";
    }

    @PostMapping("/projects/{id}/members")
    public String addMemberToProject(@PathVariable Integer id,
                                     @Valid @ModelAttribute("memberForm") ProjectMemberAddDto memberForm,
                                     BindingResult bindingResult,
                                     Model model,
                                     Principal principal) {

        Optional<Project> projectOptional = projectService.findById(id);

        if (projectOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден");
        }

        Project project = projectOptional.get();

        boolean canManageProject = projectService.canManageProject(id, principal.getName());

        if (!canManageProject) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("roles", getProjectRoles());
            return "project-member-form";
        }

        try {
            projectService.addMemberToProject(id, memberForm, principal.getName());
        } catch (IllegalStateException exception) {
            model.addAttribute("project", project);
            model.addAttribute("roles", getProjectRoles());
            model.addAttribute("errorMessage", exception.getMessage());
            return "project-member-form";
        }

        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{projectId}/members/{userId}/delete")
    public String removeMemberFromProject(@PathVariable Integer projectId,
                                          @PathVariable Integer userId,
                                          Principal principal) {
        try {
            projectService.removeMemberFromProject(projectId, userId, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + projectId;
    }

    private List<RoleType> getProjectRoles() {
        List<RoleType> roles = new ArrayList<>();

        roles.add(RoleType.PROJECT_ORGANIZATION_ADMIN);
        roles.add(RoleType.CONTROL_ADMIN);
        roles.add(RoleType.REQUEST_INITIATOR);
        roles.add(RoleType.WORK_EXECUTOR);

        return roles;
    }

    @GetMapping("/projects/{id}/board")
    public String showProjectBoard(@PathVariable Integer id,
                                   Model model,
                                   Principal principal) {
        Optional<Project> projectOptional = projectService.findById(id);

        if (projectOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден");
        }

        Project project = projectOptional.get();

        if (!projectService.isProjectMember(id, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        model.addAttribute("project", project);
        model.addAttribute("boardStatuses", getBoardStatuses());
        model.addAttribute("taskStatuses", TaskStatusType.values());
        model.addAttribute("tasksByStatus", taskService.findTasksByBoardStatusForProject(id));
        model.addAttribute("taskStatusLabels", getTaskStatusLabels());
        model.addAttribute("canManageProject", projectService.canManageProject(id, principal.getName()));

        return "project-board";
    }

    private List<TaskStatusType> getBoardStatuses() {
        List<TaskStatusType> statuses = new ArrayList<>();

        statuses.add(TaskStatusType.TO_DO);
        statuses.add(TaskStatusType.IN_PROGRESS);
        statuses.add(TaskStatusType.ON_REVIEW);
        statuses.add(TaskStatusType.NEEDS_REVISION);
        statuses.add(TaskStatusType.DONE);

        return statuses;
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
}
