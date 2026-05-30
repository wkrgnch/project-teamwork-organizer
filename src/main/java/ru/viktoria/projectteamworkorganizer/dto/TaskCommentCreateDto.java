package ru.viktoria.projectteamworkorganizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskCommentType;

public class TaskCommentCreateDto {

    @NotNull(message = "Тип комментария обязателен")
    private TaskCommentType commentType;

    @NotBlank(message = "Текст комментария обязателен")
    private String text;

    public TaskCommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(TaskCommentType commentType) {
        this.commentType = commentType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
