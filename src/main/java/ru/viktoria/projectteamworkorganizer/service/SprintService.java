package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.SprintCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;

public interface SprintService {

    Sprint createSprint(Integer projectId, SprintCreateDto sprintCreateDto, String currentUsername);
}