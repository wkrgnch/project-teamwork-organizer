package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.TaskCommentCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.TaskComment;

import java.util.List;

public interface TaskCommentService {

    List<TaskComment> findCommentsByTaskId(Integer taskId);

    void addComment(Integer taskId, TaskCommentCreateDto commentCreateDto, String currentUsername);
}
