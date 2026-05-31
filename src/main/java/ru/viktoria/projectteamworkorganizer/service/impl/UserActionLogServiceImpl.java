package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.entity.UserActionLog;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;
import ru.viktoria.projectteamworkorganizer.repository.UserActionLogRepository;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;
import ru.viktoria.projectteamworkorganizer.service.UserActionLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserActionLogServiceImpl implements UserActionLogService {

    private final UserActionLogRepository userActionLogRepository;
    private final UserRepository userRepository;

    public UserActionLogServiceImpl(UserActionLogRepository userActionLogRepository,
                                    UserRepository userRepository) {
        this.userActionLogRepository = userActionLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void log(String username,
                    UserActionType actionType,
                    ActionObjectType objectType,
                    Integer objectId,
                    String objectName,
                    String description) {
        UserActionLog log = new UserActionLog();

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            log.setActorUser(userOptional.get());
        }

        log.setActionType(actionType);
        log.setObjectType(objectType);
        log.setObjectId(objectId);
        log.setObjectName(objectName);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        userActionLogRepository.save(log);
    }

    @Override
    public List<UserActionLog> findLatestLogs() {
        return userActionLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
