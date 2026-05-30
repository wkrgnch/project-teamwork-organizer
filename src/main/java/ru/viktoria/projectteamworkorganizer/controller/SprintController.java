package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.SprintCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.SprintService;

import java.security.Principal;
import java.util.Optional;

@Controller
public class SprintController {

    private final SprintService sprintService;
    private final ProjectService projectService;

    public SprintController(SprintService sprintService,
                            ProjectService projectService) {
        this.sprintService = sprintService;
        this.projectService = projectService;
    }

    @GetMapping("/projects/{projectId}/sprints/new")
    public String showCreateSprintForm(@PathVariable Integer projectId,
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

        if (project.getMethodology() != ProjectMethodologyType.SCRUM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Спринты доступны только для Scrum-проектов");
        }

        model.addAttribute("project", project);
        model.addAttribute("sprintForm", new SprintCreateDto());

        return "sprint-form";
    }

    @PostMapping("/projects/{projectId}/sprints")
    public String createSprint(@PathVariable Integer projectId,
                               @Valid @ModelAttribute("sprintForm") SprintCreateDto sprintForm,
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

        if (project.getMethodology() != ProjectMethodologyType.SCRUM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Спринты доступны только для Scrum-проектов");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            return "sprint-form";
        }

        try {
            sprintService.createSprint(projectId, sprintForm, principal.getName());
        } catch (IllegalStateException exception) {
            model.addAttribute("project", project);
            model.addAttribute("errorMessage", exception.getMessage());
            return "sprint-form";
        }

        return "redirect:/projects/" + projectId;
    }
}