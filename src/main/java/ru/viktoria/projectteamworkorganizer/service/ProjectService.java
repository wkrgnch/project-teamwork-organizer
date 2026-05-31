package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectMemberAddDto;
import ru.viktoria.projectteamworkorganizer.entity.Project;
import ru.viktoria.projectteamworkorganizer.entity.ProjectMember;
import ru.viktoria.projectteamworkorganizer.entity.ProjectStage;
import ru.viktoria.projectteamworkorganizer.entity.Sprint;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStatusType;

import java.util.List;
import java.util.Optional;

public interface ProjectService {

    List<Project> findAll();

    Project create(ProjectCreateDto projectCreateDto, String username);

    List<ProjectMember> findMembersByProjectId(Integer projectId);

    List<ProjectMember> findExecutorsByProjectId(Integer projectId);

    Optional<Project> findById(Integer id);

    List<ProjectStage> findStagesByProjectId(Integer projectId);

    List<Sprint> findSprintsByProjectId(Integer projectId);

    boolean canManageProject(Integer projectId, String username);

    boolean isProjectMember(Integer projectId, String username);

    void addMemberToProject(Integer projectId, ProjectMemberAddDto memberAddDto, String currentUsername);

    List<Project> findProjectsForUser(String username);

    void removeMemberFromProject(Integer projectId, Integer userId, String currentUsername);

    void changeProjectStatus(Integer projectId, ProjectStatusType status, String currentUsername);
}
