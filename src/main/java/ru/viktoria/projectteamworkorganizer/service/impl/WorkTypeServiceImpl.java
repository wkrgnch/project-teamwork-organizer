package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.WorkTypeCreateDto;
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

    @Override
    @Transactional
    public WorkType createWorkType(WorkTypeCreateDto workTypeCreateDto) {
        String type = workTypeCreateDto.getType().trim();

        if (workTypeRepository.existsByTypeIgnoreCase(type)) {
            throw new IllegalStateException("Тип работы с таким названием уже существует");
        }

        WorkType workType = new WorkType();

        workType.setType(type);
        workType.setDescription(workTypeCreateDto.getDescription().trim());

        return workTypeRepository.save(workType);
    }

    @Override
    @Transactional
    public void deleteWorkType(Integer id) {
        if (!workTypeRepository.existsById(id)) {
            throw new IllegalStateException("Тип работы не найден");
        }

        try {
            workTypeRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Нельзя удалить тип работы, который уже используется в задачах");
        }
    }
}