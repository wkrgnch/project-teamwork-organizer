package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;

public class ProjectMemberAddDto {

    @NotBlank(message = "Логин пользователя обязателен")
    private String username;

    @NotNull(message = "Роль обязательна")
    private RoleType roleType;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }
}