package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.viktoria.projectteamworkorganizer.dto.UserRegisterDto;
import ru.viktoria.projectteamworkorganizer.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new UserRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userForm") UserRegisterDto userForm,
                               BindingResult bindingResult) {

        if (userService.existsByUsername(userForm.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Пользователь с таким логином уже существует");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(userForm);

        return "redirect:/login?registered";
    }
}