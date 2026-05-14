package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.*;

public class UserRegisterDto {
    @NotBlank(message = "ФИО обязательно")
    @Size(max = 150, message = "ФИО не должно быть длиннее 150 символов")
    private String fullName;

    @Email(message = "Некорректный email")
    @Size(max = 150, message = "Email не должен быть длиннее 150 символов")
    private String email;

    @NotBlank(message = "Логин обязателен")
    @Size(max = 50, message = "Логин не должен быть длиннее 50 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
