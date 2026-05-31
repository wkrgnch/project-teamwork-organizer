package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestClarificationDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestDecisionDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RequestStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;
import ru.viktoria.projectteamworkorganizer.repository.ProjectMemberRepository;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRepository;
import ru.viktoria.projectteamworkorganizer.repository.ProjectRequestRepository;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;
import ru.viktoria.projectteamworkorganizer.service.ProjectRequestService;
import ru.viktoria.projectteamworkorganizer.service.UserActionLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private final ProjectRequestRepository projectRequestRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserActionLogService userActionLogService;

    public ProjectRequestServiceImpl(ProjectRequestRepository projectRequestRepository,
                                     ProjectRepository projectRepository,
                                     UserRepository userRepository,
                                     ProjectMemberRepository projectMemberRepository,
                                     UserActionLogService userActionLogService) {
        this.projectRequestRepository = projectRequestRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userActionLogService = userActionLogService;
    }

    @Override
    public List<ProjectRequest> findVisibleRequestsForProject(Integer projectId, String currentUsername) {
        if (canManageRequests(projectId, currentUsername)) {
            return projectRequestRepository.findRequestsByProjectIdExcludingStatuses(
                    projectId,
                    List.of(
                            RequestStatusType.CANCELED,
                            RequestStatusType.REJECTED,
                            RequestStatusType.CONVERTED_TO_TASK
                    )
            );
        }

        if (canCreateRequest(projectId, currentUsername)) {
            return projectRequestRepository.findRequestsByProjectIdAndAuthorUsername(projectId, currentUsername);
        }

        throw new IllegalStateException("Нет доступа к заявкам проекта");
    }

    @Override
    public Optional<ProjectRequest> findVisibleRequest(Integer requestId, String currentUsername) {
        Optional<ProjectRequest> requestOptional = projectRequestRepository.findDetailedById(requestId);

        if (requestOptional.isEmpty()) {
            return Optional.empty();
        }

        ProjectRequest request = requestOptional.get();
        Integer projectId = request.getProject().getId();

        boolean author = request.getAuthor() != null
                && request.getAuthor().getUsername().equals(currentUsername);

        if (request.getStatus() == RequestStatusType.CANCELED) {
            if (author) {
                return requestOptional;
            }

            throw new IllegalStateException("Заявка отменена инициатором");
        }

        if (canManageRequests(projectId, currentUsername)) {
            return requestOptional;
        }

        if (author) {
            return requestOptional;
        }

        throw new IllegalStateException("Нет доступа к заявке");
    }

    @Override
    public boolean canCreateRequest(Integer projectId, String currentUsername) {
        return hasProjectRole(projectId, currentUsername, RoleType.REQUEST_INITIATOR);
    }

    @Override
    public boolean canManageRequests(Integer projectId, String currentUsername) {
        return hasProjectRole(projectId, currentUsername, RoleType.PROJECT_ORGANIZATION_ADMIN);
    }

    @Override
    @Transactional
    public ProjectRequest createRequest(Integer projectId,
                                        ProjectRequestCreateDto requestCreateDto,
                                        String currentUsername) {
        if (!canCreateRequest(projectId, currentUsername)) {
            throw new IllegalStateException("Нет прав для создания заявки в этом проекте");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("Проект не найден"));

        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Текущий пользователь не найден"));

        ProjectRequest request = new ProjectRequest();
        request.setProject(project);
        request.setAuthor(author);
        request.setTitle(requestCreateDto.getTitle().trim());
        request.setDescription(requestCreateDto.getDescription().trim());
        request.setGoal(trimToNull(requestCreateDto.getGoal()));
        request.setExpectedResult(requestCreateDto.getExpectedResult().trim());
        request.setDesiredDeadline(requestCreateDto.getDesiredDeadline());
        request.setMaterialUrl(trimToNull(requestCreateDto.getMaterialUrl()));
        request.setStatus(RequestStatusType.NEW);
        request.setCreatedAt(LocalDateTime.now());

        ProjectRequest savedRequest = projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.CREATE_REQUEST,
                ActionObjectType.REQUEST,
                savedRequest.getId(),
                savedRequest.getTitle(),
                "Создана заявка \"" + savedRequest.getTitle() + "\" в проекте \"" + project.getName() + "\""
        );

        return savedRequest;
    }

    @Override
    @Transactional
    public void addClarification(Integer requestId,
                                 ProjectRequestClarificationDto clarificationDto,
                                 String currentUsername) {
        ProjectRequest request = findDetailedRequest(requestId);

        if (request.getAuthor() == null || !request.getAuthor().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Уточнить заявку может только её инициатор");
        }

        if (request.getStatus() != RequestStatusType.NEEDS_CLARIFICATION) {
            throw new IllegalStateException("Уточнение доступно только для заявки со статусом 'Требует уточнения'");
        }

        request.setClarification(clarificationDto.getClarification().trim());
        request.setStatus(RequestStatusType.NEW);
        request.setUpdatedAt(LocalDateTime.now());

        projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_REQUEST,
                ActionObjectType.REQUEST,
                request.getId(),
                request.getTitle(),
                "Инициатор уточнил заявку \"" + request.getTitle() + "\""
        );
    }

    @Override
    @Transactional
    public void requestClarification(Integer requestId,
                                     ProjectRequestDecisionDto decisionDto,
                                     String currentUsername) {
        ProjectRequest request = findDetailedRequest(requestId);
        Integer projectId = request.getProject().getId();

        if (!canManageRequests(projectId, currentUsername)) {
            throw new IllegalStateException("Нет прав для обработки заявки");
        }

        if (request.getStatus() != RequestStatusType.NEW && request.getStatus() != RequestStatusType.NEEDS_CLARIFICATION) {
            throw new IllegalStateException("Запросить уточнение можно только по новой заявке или заявке на уточнении");
        }

        request.setStatus(RequestStatusType.NEEDS_CLARIFICATION);
        request.setClarification(trimToNull(decisionDto.getClarification()));
        request.setUpdatedAt(LocalDateTime.now());

        projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_REQUEST,
                ActionObjectType.REQUEST,
                request.getId(),
                request.getTitle(),
                "По заявке \"" + request.getTitle() + "\" запрошено уточнение"
        );
    }

    @Override
    @Transactional
    public ProjectRequest acceptRequest(Integer requestId, String currentUsername) {
        ProjectRequest request = findDetailedRequest(requestId);
        Integer projectId = request.getProject().getId();

        if (!canManageRequests(projectId, currentUsername)) {
            throw new IllegalStateException("Нет прав для обработки заявки");
        }

        if (request.getStatus() != RequestStatusType.NEW && request.getStatus() != RequestStatusType.NEEDS_CLARIFICATION) {
            throw new IllegalStateException("Принять можно только новую заявку или заявку на уточнении");
        }

        request.setStatus(RequestStatusType.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());

        ProjectRequest savedRequest = projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_REQUEST,
                ActionObjectType.REQUEST,
                request.getId(),
                request.getTitle(),
                "Заявка \"" + request.getTitle() + "\" принята"
        );

        return savedRequest;
    }

    @Override
    @Transactional
    public void rejectRequest(Integer requestId,
                              ProjectRequestDecisionDto decisionDto,
                              String currentUsername) {
        ProjectRequest request = findDetailedRequest(requestId);
        Integer projectId = request.getProject().getId();

        if (!canManageRequests(projectId, currentUsername)) {
            throw new IllegalStateException("Нет прав для обработки заявки");
        }

        if (request.getStatus() != RequestStatusType.NEW && request.getStatus() != RequestStatusType.NEEDS_CLARIFICATION) {
            throw new IllegalStateException("Отклонить можно только новую заявку или заявку на уточнении");
        }

        request.setStatus(RequestStatusType.REJECTED);
        request.setClarification(trimToNull(decisionDto.getClarification()));
        request.setUpdatedAt(LocalDateTime.now());

        projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_REQUEST,
                ActionObjectType.REQUEST,
                request.getId(),
                request.getTitle(),
                "Заявка \"" + request.getTitle() + "\" отклонена"
        );
    }

    @Override
    @Transactional
    public void cancelRequest(Integer requestId, String currentUsername) {
        ProjectRequest request = findDetailedRequest(requestId);

        if (request.getAuthor() == null || !request.getAuthor().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Отменить заявку может только её инициатор");
        }

        if (request.getStatus() != RequestStatusType.NEW
                && request.getStatus() != RequestStatusType.NEEDS_CLARIFICATION) {
            throw new IllegalStateException("Заявку нельзя отменить после принятия или отклонения");
        }

        request.setStatus(RequestStatusType.CANCELED);
        request.setUpdatedAt(LocalDateTime.now());

        projectRequestRepository.save(request);

        userActionLogService.log(
                currentUsername,
                UserActionType.UPDATE_REQUEST,
                ActionObjectType.REQUEST,
                request.getId(),
                request.getTitle(),
                "Инициатор отменил заявку \"" + request.getTitle() + "\""
        );
    }

    private ProjectRequest findDetailedRequest(Integer requestId) {
        return projectRequestRepository.findDetailedById(requestId)
                .orElseThrow(() -> new IllegalStateException("Заявка не найдена"));
    }

    private boolean hasProjectRole(Integer projectId, String username, RoleType roleType) {
        long count = projectMemberRepository.countMemberRole(projectId, username, roleType);
        return count > 0;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
