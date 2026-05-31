package ru.viktoria.projectteamworkorganizer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.repository.ProjectStageRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class PublicProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectStageRepository projectStageRepository;

    public PublicProjectController(ProjectRepository projectRepository,
                                   ProjectStageRepository projectStageRepository) {
        this.projectRepository = projectRepository;
        this.projectStageRepository = projectStageRepository;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("publicProjects", projectRepository.findTop6ByPublicProjectTrueOrderByCreatedAtDesc());
        return "home";
    }

    @GetMapping("/public/projects")
    public String showPublicProjects(@RequestParam(name = "query", required = false) String query,
                                     Model model) {
        List<Project> projects;

        if (query == null || query.trim().isEmpty()) {
            projects = projectRepository.findByPublicProjectTrueOrderByCreatedAtDesc();
        } else {
            projects = projectRepository.searchPublicProjectsByName(query.trim());
        }

        model.addAttribute("projects", projects);
        model.addAttribute("query", query);

        return "public-projects";
    }

    @GetMapping("/public/projects/{id}")
    public String showPublicProjectDetails(@PathVariable Integer id,
                                           Model model) {
        Optional<Project> projectOptional = projectRepository.findByIdAndPublicProjectTrue(id);

        if (projectOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Публичный проект не найден");
        }

        Project project = projectOptional.get();

        model.addAttribute("project", project);
        model.addAttribute("stages", projectStageRepository.findByProjectIdOrderByOrderNumberAsc(id));

        return "public-project-details";
    }
}
