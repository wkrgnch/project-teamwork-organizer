package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskPriorityType;

import java.time.LocalDate;

public class TaskCreateDto {

    private Integer requestId;

    @NotBlank(message = "Название задачи обязательно")
    private String title;

    @NotBlank(message = "Описание задачи обязательно")
    private String description;

    @NotNull(message = "Этап проекта обязателен")
    private Integer stageId;

    private Integer sprintId;

    @NotNull(message = "Тип работы обязателен")
    private Integer workTypeId;

    @NotNull(message = "Приоритет обязателен")
    private TaskPriorityType priority;

    private Integer assigneeId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadline;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

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

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }

    public Integer getSprintId() {
        return sprintId;
    }

    public void setSprintId(Integer sprintId) {
        this.sprintId = sprintId;
    }

    public Integer getWorkTypeId() {
        return workTypeId;
    }

    public void setWorkTypeId(Integer workTypeId) {
        this.workTypeId = workTypeId;
    }

    public TaskPriorityType getPriority() {
        return priority;
    }

    public void setPriority(TaskPriorityType priority) {
        this.priority = priority;
    }

    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
