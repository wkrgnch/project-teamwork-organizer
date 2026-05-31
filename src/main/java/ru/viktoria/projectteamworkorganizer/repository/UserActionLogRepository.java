package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.UserActionLog;

import java.util.List;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Integer> {

    @EntityGraph(attributePaths = "actorUser")
    List<UserActionLog> findTop100ByOrderByCreatedAtDesc();
}
