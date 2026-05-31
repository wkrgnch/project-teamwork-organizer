package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ProjectRequestCreateDto {

    @NotBlank(message = "Название заявки обязательно")
    @Size(max = 150, message = "Название заявки не должно превышать 150 символов")
    private String title;

    @NotBlank(message = "Описание заявки обязательно")
    private String description;

    private String goal;

    @NotBlank(message = "Ожидаемый результат обязателен")
    private String expectedResult;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desiredDeadline;

    private String materialUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public LocalDate getDesiredDeadline() {
        return desiredDeadline;
    }

    public void setDesiredDeadline(LocalDate desiredDeadline) {
        this.desiredDeadline = desiredDeadline;
    }

    public String getMaterialUrl() {
        return materialUrl;
    }

    public void setMaterialUrl(String materialUrl) {
        this.materialUrl = materialUrl;
    }
}
