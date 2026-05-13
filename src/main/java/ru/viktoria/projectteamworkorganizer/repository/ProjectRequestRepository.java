package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;

public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Integer> {
}