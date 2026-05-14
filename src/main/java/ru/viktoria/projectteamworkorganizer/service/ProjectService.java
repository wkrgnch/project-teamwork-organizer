package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.entity.Project;

import java.util.List;


public interface ProjectService {
    List<Project> findAll();
}
