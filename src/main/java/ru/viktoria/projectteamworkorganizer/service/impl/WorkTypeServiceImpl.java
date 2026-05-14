package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import ru.viktoria.projectteamworkorganizer.entity.WorkType;
import ru.viktoria.projectteamworkorganizer.repository.WorkTypeRepository;
import ru.viktoria.projectteamworkorganizer.service.WorkTypeService;

import java.util.List;

@Service
public class WorkTypeServiceImpl implements WorkTypeService {

    private final WorkTypeRepository workTypeRepository;

    public WorkTypeServiceImpl(WorkTypeRepository workTypeRepository) {
        this.workTypeRepository = workTypeRepository;
    }

    @Override
    public List<WorkType> findAll() {
        return workTypeRepository.findAll();
    }
}
