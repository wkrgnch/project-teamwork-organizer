package ru.viktoria.projectteamworkorganizer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.viktoria.projectteamworkorganizer.service.WorkTypeService;

@Controller
public class WorkTypeController {

    private final WorkTypeService workTypeService;
    public WorkTypeController(WorkTypeService workTypeService) {
        this.workTypeService = workTypeService;
    }

    @GetMapping("/work-types")
    public String showAllWorkTypes(Model model) {
        model.addAttribute("workTypes", workTypeService.findAll());
        return "work-types";
    }
}
