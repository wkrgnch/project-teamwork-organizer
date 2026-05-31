package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectRequestClarificationDto {

    @NotBlank(message = "Текст уточнения обязателен")
    private String clarification;

    public String getClarification() {
        return clarification;
    }

    public void setClarification(String clarification) {
        this.clarification = clarification;
    }
}
