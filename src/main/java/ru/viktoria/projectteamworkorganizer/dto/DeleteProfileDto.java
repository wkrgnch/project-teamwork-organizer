package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;

public class DeleteProfileDto {

    @NotBlank(message = "Для удаления профиля введите текущий пароль")
    private String currentPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
