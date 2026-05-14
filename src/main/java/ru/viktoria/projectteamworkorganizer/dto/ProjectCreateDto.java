package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;

import java.time.LocalDate;

public class ProjectCreateDto {
    @NotBlank(message = "Название проекта обязательно")
    private String name;

    private String description;

    private String publicDescription;

    @NotNull(message = "Методология проекта обязательна")
    private ProjectMethodologyType methodology;

    @NotNull(message = "Дата начала обязательна")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Boolean publicProject = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicDescription() {
        return publicDescription;
    }

    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    public ProjectMethodologyType getMethodology() {
        return methodology;
    }

    public void setMethodology(ProjectMethodologyType methodology) {
        this.methodology = methodology;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getPublicProject() {
        return publicProject;
    }

    public void setPublicProject(Boolean publicProject) {
        this.publicProject = publicProject;
    }
}
