package ru.viktoria.projectteamworkorganizer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.repository.TaskRepository;
import ru.viktoria.projectteamworkorganizer.repository.UserActionLogRepository;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserActionLogRepository userActionLogRepository;

    public AdminController(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           UserActionLogRepository userActionLogRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userActionLogRepository = userActionLogRepository;
    }

    @GetMapping("/admin")
    public String showAdminDashboard(Model model) {
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("projectsCount", projectRepository.count());
        model.addAttribute("tasksCount", taskRepository.count());
        model.addAttribute("logsCount", userActionLogRepository.count());

        return "admin-dashboard";
    }
}
