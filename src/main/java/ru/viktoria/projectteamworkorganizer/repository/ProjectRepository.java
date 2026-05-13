package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
