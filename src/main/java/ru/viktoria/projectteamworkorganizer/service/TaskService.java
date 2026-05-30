package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Task;
import ru.viktoria.projectteamworkorganizer.entity.enums.TaskStatusType;

import java.util.List;
import java.util.Map;

public interface TaskService {

    List<Task> findTasksByProjectId(Integer projectId);

    Task createTask(Integer projectId, TaskCreateDto taskCreateDto, String currentUsername);

    Integer changeStatus(Integer taskId, TaskStatusType status, String currentUsername);

    Map<TaskStatusType, List<Task>> findTasksByBoardStatusForProject(Integer projectId);
}