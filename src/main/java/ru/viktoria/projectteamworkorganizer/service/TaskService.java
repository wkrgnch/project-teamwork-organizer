package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.TaskCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Task;

import java.util.List;
import java.util.Map;

public interface TaskService {

    List<Task> findTasksByProjectId(Integer projectId);

    Task createTask(Integer projectId, TaskCreateDto taskCreateDto, String currentUsername);

    Map<Integer, List<Task>> findTasksByStageForProject(Integer projectId);

    Integer changeStage(Integer taskId, Integer stageId, String currentUsername);
}