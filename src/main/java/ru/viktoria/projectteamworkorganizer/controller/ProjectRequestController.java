package ru.viktoria.projectteamworkorganizer.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestClarificationDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectRequestDecisionDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;
import ru.viktoria.projectteamworkorganizer.entity.enums.RequestStatusType;
import ru.viktoria.projectteamworkorganizer.service.ProjectRequestService;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProjectRequestController {

    private final ProjectRequestService projectRequestService;
    private final ProjectService projectService;

    public ProjectRequestController(ProjectRequestService projectRequestService,
                                    ProjectService projectService) {
        this.projectRequestService = projectRequestService;
        this.projectService = projectService;
    }

    @GetMapping("/projects/{projectId}/requests")
    public String showProjectRequests(@PathVariable Integer projectId,
                                      Model model,
                                      Principal principal) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        try {
            model.addAttribute("project", project);
            model.addAttribute("requests", projectRequestService.findVisibleRequestsForProject(projectId, principal.getName()));
            model.addAttribute("requestStatusLabels", getRequestStatusLabels());
            model.addAttribute("canCreateRequest", projectRequestService.canCreateRequest(projectId, principal.getName()));
            model.addAttribute("canManageRequests", projectRequestService.canManageRequests(projectId, principal.getName()));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        return "project-requests";
    }

    @GetMapping("/projects/{projectId}/requests/new")
    public String showCreateRequestForm(@PathVariable Integer projectId,
                                        Model model,
                                        Principal principal) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        if (!projectRequestService.canCreateRequest(projectId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет прав для создания заявки");
        }

        model.addAttribute("project", project);
        model.addAttribute("requestForm", new ProjectRequestCreateDto());

        return "project-request-form";
    }

    @PostMapping("/projects/{projectId}/requests")
    public String createRequest(@PathVariable Integer projectId,
                                @Valid @ModelAttribute("requestForm") ProjectRequestCreateDto requestForm,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        if (!projectRequestService.canCreateRequest(projectId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет прав для создания заявки");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            return "project-request-form";
        }

        try {
            projectRequestService.createRequest(projectId, requestForm, principal.getName());
        } catch (IllegalStateException exception) {
            model.addAttribute("project", project);
            model.addAttribute("errorMessage", exception.getMessage());
            return "project-request-form";
        }

        return "redirect:/projects/" + projectId + "/requests";
    }

    @GetMapping("/requests/{requestId}")
    public String showRequestDetails(@PathVariable Integer requestId,
                                     Model model,
                                     Principal principal) {
        ProjectRequest request = findVisibleRequestOrThrow(requestId, principal.getName());
        Integer projectId = request.getProject().getId();

        boolean canManageRequests = projectRequestService.canManageRequests(projectId, principal.getName());
        boolean isAuthor = request.getAuthor() != null
                && request.getAuthor().getUsername().equals(principal.getName());

        model.addAttribute("request", request);
        model.addAttribute("project", request.getProject());
        model.addAttribute("requestStatusLabels", getRequestStatusLabels());
        model.addAttribute("clarificationForm", new ProjectRequestClarificationDto());
        model.addAttribute("decisionForm", new ProjectRequestDecisionDto());
        model.addAttribute("canManageRequests", canManageRequests);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("canAddClarification", isAuthor && request.getStatus() == RequestStatusType.NEEDS_CLARIFICATION);
        model.addAttribute("canCancelRequest", isAuthor && (request.getStatus() == RequestStatusType.NEW
                || request.getStatus() == RequestStatusType.NEEDS_CLARIFICATION));
        model.addAttribute("canProcessRequest", canManageRequests && (request.getStatus() == RequestStatusType.NEW
                || request.getStatus() == RequestStatusType.NEEDS_CLARIFICATION));

        return "project-request-details";
    }

    @PostMapping("/requests/{requestId}/clarification")
    public String addClarification(@PathVariable Integer requestId,
                                   @Valid @ModelAttribute("clarificationForm") ProjectRequestClarificationDto clarificationForm,
                                   BindingResult bindingResult,
                                   Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/requests/" + requestId;
        }

        try {
            projectRequestService.addClarification(requestId, clarificationForm, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/requests/" + requestId;
    }

    @PostMapping("/requests/{requestId}/request-clarification")
    public String requestClarification(@PathVariable Integer requestId,
                                       @ModelAttribute("decisionForm") ProjectRequestDecisionDto decisionForm,
                                       Principal principal) {
        try {
            projectRequestService.requestClarification(requestId, decisionForm, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/requests/" + requestId;
    }

    @PostMapping("/requests/{requestId}/accept")
    public String acceptRequest(@PathVariable Integer requestId,
                                Principal principal) {
        ProjectRequest request;

        try {
            request = projectRequestService.acceptRequest(requestId, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + request.getProject().getId() + "/tasks/new?requestId=" + request.getId();
    }

    @PostMapping("/requests/{requestId}/reject")
    public String rejectRequest(@PathVariable Integer requestId,
                                @ModelAttribute("decisionForm") ProjectRequestDecisionDto decisionForm,
                                Principal principal) {
        ProjectRequest request = findVisibleRequestOrThrow(requestId, principal.getName());
        Integer projectId = request.getProject().getId();

        try {
            projectRequestService.rejectRequest(requestId, decisionForm, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + projectId + "/requests";
    }

    @PostMapping("/requests/{requestId}/cancel")
    public String cancelRequest(@PathVariable Integer requestId,
                                Principal principal) {
        ProjectRequest request = findVisibleRequestOrThrow(requestId, principal.getName());
        Integer projectId = request.getProject().getId();

        try {
            projectRequestService.cancelRequest(requestId, principal.getName());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        return "redirect:/projects/" + projectId + "/requests";
    }

    private ProjectRequest findVisibleRequestOrThrow(Integer requestId, String currentUsername) {
        Optional<ProjectRequest> requestOptional;

        try {
            requestOptional = projectRequestService.findVisibleRequest(requestId, currentUsername);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }

        return requestOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));
    }

    private Map<RequestStatusType, String> getRequestStatusLabels() {
        Map<RequestStatusType, String> labels = new HashMap<>();

        labels.put(RequestStatusType.NEW, "Новая");
        labels.put(RequestStatusType.NEEDS_CLARIFICATION, "Требует уточнения");
        labels.put(RequestStatusType.REVIEWED, "Рассмотрена");
        labels.put(RequestStatusType.ACCEPTED, "Принята");
        labels.put(RequestStatusType.IN_WORK, "В работе");
        labels.put(RequestStatusType.ON_REVIEW, "На проверке");
        labels.put(RequestStatusType.COMPLETED, "Завершена");
        labels.put(RequestStatusType.REJECTED, "Отклонена");
        labels.put(RequestStatusType.CANCELED, "Отменена");
        labels.put(RequestStatusType.CONVERTED_TO_TASK, "Создана задача");

        return labels;
    }
}
