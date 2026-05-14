package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.entity.WorkType;
import java.util.List;

public interface WorkTypeService {

    List<WorkType> findAll();
}