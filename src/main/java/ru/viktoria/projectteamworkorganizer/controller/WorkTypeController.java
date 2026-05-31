package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.viktoria.projectteamworkorganizer.dto.WorkTypeCreateDto;
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
        model.addAttribute("workTypeForm", new WorkTypeCreateDto());

        return "work-types";
    }

    @PostMapping("/work-types")
    public String createWorkType(@Valid @ModelAttribute("workTypeForm") WorkTypeCreateDto workTypeForm,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("workTypes", workTypeService.findAll());
            return "work-types";
        }

        try {
            workTypeService.createWorkType(workTypeForm);
        } catch (IllegalStateException exception) {
            model.addAttribute("workTypes", workTypeService.findAll());
            model.addAttribute("errorMessage", exception.getMessage());
            return "work-types";
        }

        return "redirect:/work-types";
    }

    @PostMapping("/work-types/{id}/delete")
    public String deleteWorkType(@PathVariable Integer id,
                                 Model model) {
        try {
            workTypeService.deleteWorkType(id);
        } catch (IllegalStateException exception) {
            model.addAttribute("workTypes", workTypeService.findAll());
            model.addAttribute("workTypeForm", new WorkTypeCreateDto());
            model.addAttribute("errorMessage", exception.getMessage());
            return "work-types";
        }

        return "redirect:/work-types";
    }
}