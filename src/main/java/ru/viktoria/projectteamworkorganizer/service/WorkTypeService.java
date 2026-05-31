package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.WorkTypeCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.WorkType;

import java.util.List;

public interface WorkTypeService {

    List<WorkType> findAll();

    WorkType createWorkType(WorkTypeCreateDto workTypeCreateDto);

    void deleteWorkType(Integer id);
}