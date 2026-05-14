package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;

@Controller
public class ProjectController {
    private ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public String showProjects(Model model){
        model.addAttribute("projects", projectService.findAll());
        return "projects";
    }

    @GetMapping("/projects/new")
    public String showCreateProjectForm(Model model){
        model.addAttribute("projectForm", new ProjectCreateDto());
        model.addAttribute("methodologies", ProjectMethodologyType.values());
        return "project-form";
    }

    @PostMapping("/projects")
    public String createProject(
            @Valid
            @ModelAttribute("projectForm")
            ProjectCreateDto projectForm, BindingResult bindingResult, Model model
    ){
        if (bindingResult.hasErrors()){
            model.addAttribute("methodologies", ProjectMethodologyType.values());
            return "project-form";
        }
        projectService.create(projectForm);
        return "redirect:/projects";
    }
}
