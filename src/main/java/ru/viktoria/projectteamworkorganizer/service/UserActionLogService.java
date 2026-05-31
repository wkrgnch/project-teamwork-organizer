package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.entity.UserActionLog;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;

import java.util.List;

public interface UserActionLogService {

    void log(String username,
             UserActionType actionType,
             ActionObjectType objectType,
             Integer objectId,
             String objectName,
             String description);

    List<UserActionLog> findLatestLogs();
}
