package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.ChangePasswordDto;
import ru.viktoria.projectteamworkorganizer.dto.DeleteProfileDto;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.service.UserProfileService;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/profile/password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordForm", new ChangePasswordDto());
        return "change-password";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordDto passwordForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal) {
        if (bindingResult.hasErrors()) {
            return "change-password";
        }

        try {
            userProfileService.changePassword(principal.getName(), passwordForm);
        } catch (IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "change-password";
        }

        return "redirect:/profile?passwordChanged";
    }

    @GetMapping("/profile/delete")
    public String showDeleteProfileForm(Model model) {
        model.addAttribute("deleteForm", new DeleteProfileDto());
        return "delete-profile";
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(@Valid @ModelAttribute("deleteForm") DeleteProfileDto deleteForm,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal,
                                HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "delete-profile";
        }

        try {
            userProfileService.deleteProfile(principal.getName(), deleteForm);
        } catch (IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "delete-profile";
        }

        request.getSession().invalidate();
        return "redirect:/login?profileDeleted";
    }

    private User getCurrentUser(Principal principal) {
        Optional<User> userOptional = userProfileService.findByUsername(principal.getName());

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }

        return userOptional.get();
    }
}
