package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WorkTypeCreateDto {

    @NotBlank(message = "Название типа работы обязательно")
    @Size(max = 100, message = "Название типа работы не должно превышать 100 символов")
    private String type;

    @NotBlank(message = "Описание типа работы обязательно")
    private String description;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}