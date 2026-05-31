package ru.viktoria.projectteamworkorganizer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.viktoria.projectteamworkorganizer.service.UserActionLogService;

@Controller
public class UserActionLogController {

    private final UserActionLogService userActionLogService;

    public UserActionLogController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    @GetMapping("/logs")
    public String showLogs(Model model) {
        model.addAttribute("logs", userActionLogService.findLatestLogs());
        return "logs";
    }
}
