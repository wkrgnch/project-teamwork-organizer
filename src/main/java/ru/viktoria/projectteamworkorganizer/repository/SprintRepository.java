package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
}
