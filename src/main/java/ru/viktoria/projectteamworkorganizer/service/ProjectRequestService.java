package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestClarificationDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestDecisionDto;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;

import java.util.List;
import java.util.Optional;

public interface ProjectRequestService {

    List<ProjectRequest> findVisibleRequestsForProject(Integer projectId, String currentUsername);

    Optional<ProjectRequest> findVisibleRequest(Integer requestId, String currentUsername);

    boolean canCreateRequest(Integer projectId, String currentUsername);

    boolean canManageRequests(Integer projectId, String currentUsername);

    ProjectRequest createRequest(Integer projectId,
                                 ProjectRequestCreateDto requestCreateDto,
                                 String currentUsername);

    void addClarification(Integer requestId,
                          ProjectRequestClarificationDto clarificationDto,
                          String currentUsername);

    void requestClarification(Integer requestId,
                              ProjectRequestDecisionDto decisionDto,
                              String currentUsername);

    ProjectRequest acceptRequest(Integer requestId, String currentUsername);

    void rejectRequest(Integer requestId,
                       ProjectRequestDecisionDto decisionDto,
                       String currentUsername);

    void cancelRequest(Integer requestId, String currentUsername);
}
