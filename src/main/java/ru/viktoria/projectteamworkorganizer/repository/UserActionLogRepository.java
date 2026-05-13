package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.UserActionLog;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Integer> {
}
