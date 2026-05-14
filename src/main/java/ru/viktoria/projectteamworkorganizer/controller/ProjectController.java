package ru.viktoria.projectteamworkorganizer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
