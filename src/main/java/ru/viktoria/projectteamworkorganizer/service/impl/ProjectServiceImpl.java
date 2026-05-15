package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.ProjectCreateDto;
import ru.viktoria.projectteamworkorganizer.dto.ProjectMemberAddDto;
import ru.viktoria.projectteamworkorganizer.entity.*;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectMethodologyType;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStageStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStatusType;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.id.ProjectMemberId;
import ru.viktoria.projectteamworkorganizer.repository.*;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private ProjectStageRepository projectStageRepository;
    private SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectStageRepository projectStageRepository,
                              SprintRepository sprintRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectStageRepository = projectStageRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public List<Project> findProjectsForUser(String username) {
        return projectRepository.findProjectsByMemberUsername(username);
    }


    @Override
    public boolean canManageProject(Integer projectId, String username) {
        long count = projectMemberRepository.countMemberRole(
                projectId,
                username,
                RoleType.PROJECT_ORGANIZATION_ADMIN
        );

        return count > 0;
    }

    @Override
    @Transactional
    public void addMemberToProject(Integer projectId,
                                   ProjectMemberAddDto memberAddDto,
                                   String currentUsername) {

        boolean canManage = canManageProject(projectId, currentUsername);

        if (!canManage) {
            throw new IllegalStateException("Нет прав для управления участниками проекта");
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isEmpty()) {
            throw new IllegalStateException("Проект не найден");
        }

        Project project = projectOptional.get();

        Optional<User> userOptional = userRepository.findByUsername(memberAddDto.getUsername());

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Пользователь с таким логином не найден");
        }

        User userToAdd = userOptional.get();

        if (userToAdd.getSystemRole() != null
                && userToAdd.getSystemRole().getType() == RoleType.GLOBAL_ADMIN) {
            throw new IllegalStateException("Глобального администратора нельзя добавить в проект");
        }

        Optional<Role> roleOptional = roleRepository.findByType(memberAddDto.getRoleType());

        if (roleOptional.isEmpty()) {
            throw new IllegalStateException("Роль не найдена");
        }

        Role role = roleOptional.get();

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, userToAdd.getId());

        Optional<ProjectMember> memberOptional = projectMemberRepository.findById(projectMemberId);

        ProjectMember projectMember;

        if (memberOptional.isPresent()) {
            projectMember = memberOptional.get();

            for (Role existingRole : projectMember.getRoles()) {
                if (existingRole.getType() == role.getType()) {
                    throw new IllegalStateException("У пользователя уже есть такая роль в этом проекте");
                }
            }
        } else {
            projectMember = new ProjectMember();

            projectMember.setId(projectMemberId);
            projectMember.setProject(project);
            projectMember.setUser(userToAdd);
            projectMember.setJoinedAt(LocalDateTime.now());
        }

        projectMember.getRoles().add(role);

        projectMemberRepository.save(projectMember);
    }

    @Override
    @Transactional
    public void removeMemberFromProject(Integer projectId, Integer userId, String currentUsername) {
        boolean canManage = canManageProject(projectId, currentUsername);

        if (!canManage) {
            throw new IllegalStateException("Нет прав для удаления участников проекта");
        }

        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);

        if (currentUserOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        User currentUser = currentUserOptional.get();

        if (currentUser.getId().equals(userId)) {
            throw new IllegalStateException("Нельзя удалить самого себя из проекта");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, userId);

        if (!projectMemberRepository.existsById(projectMemberId)) {
            throw new IllegalStateException("Участник проекта не найден");
        }

        projectMemberRepository.deleteById(projectMemberId);
    }

    @Override
    @Transactional
    public Project create(ProjectCreateDto projectCreateDto, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        User currentUser = userOptional.get();

        if (currentUser.getSystemRole() != null
                && currentUser.getSystemRole().getType() == RoleType.GLOBAL_ADMIN) {
            throw new IllegalStateException("Глобальный администратор не может создавать проекты");
        }

        Optional<Role> roleOptional = roleRepository.findByType(RoleType.PROJECT_ORGANIZATION_ADMIN);

        if (roleOptional.isEmpty()) {
            throw new IllegalStateException("Роль PROJECT_ORGANIZATION_ADMIN не найдена");
        }

        Role projectAdminRole = roleOptional.get();

        Project project = new Project();

        project.setName(projectCreateDto.getName());
        project.setDescription(projectCreateDto.getDescription());
        project.setPublicDescription(projectCreateDto.getPublicDescription());
        project.setMethodology(projectCreateDto.getMethodology());
        project.setStatus(ProjectStatusType.PLANNED);
        project.setStartDate(projectCreateDto.getStartDate());
        project.setEndDate(projectCreateDto.getEndDate());
        project.setPublicProject(Boolean.TRUE.equals(projectCreateDto.getPublicProject()));
        project.setCreatedByUser(currentUser);
        project.setCreatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        addCreatorAsProjectAdmin(savedProject, currentUser, projectAdminRole);

        if (savedProject.getMethodology() == ProjectMethodologyType.SCRUM) {
            createScrumTemplate(savedProject);
        } else if (savedProject.getMethodology() == ProjectMethodologyType.KANBAN) {
            createKanbanTemplate(savedProject);
        }

        return savedProject;
    }

    private void addCreatorAsProjectAdmin(Project project, User user, Role role) {
        ProjectMember projectMember = new ProjectMember();

        projectMember.setId(new ProjectMemberId(project.getId(), user.getId()));
        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.getRoles().add(role);
        projectMember.setJoinedAt(LocalDateTime.now());

        projectMemberRepository.save(projectMember);
    }

    private void createScrumTemplate(Project project) {
        createStage(project, "Бэклог", "Список задач, которые планируются к выполнению", 1);
        createStage(project, "Планирование спринта", "Отбор задач в ближайший спринт", 2);
        createStage(project, "Спринт в работе", "Выполнение задач текущего спринта", 3);
        createStage(project, "Проверка результата", "Проверка выполненных задач", 4);
        createStage(project, "Завершено", "Завершённые задачи и результаты", 5);

        createFirstSprint(project);
    }

    private void createKanbanTemplate(Project project) {
        createStage(project, "Новые", "Новые задачи, которые ещё не взяты в работу", 1);
        createStage(project, "В очереди", "Задачи, ожидающие выполнения", 2);
        createStage(project, "В работе", "Задачи, которые сейчас выполняются", 3);
        createStage(project, "На проверке", "Задачи, переданные на проверку", 4);
        createStage(project, "Завершено", "Выполненные задачи", 5);
    }

    private void createStage(Project project, String name,
                             String description, Integer orderNumber) {

        ProjectStage stage = new ProjectStage();

        stage.setProject(project);
        stage.setName(name);
        stage.setDescription(description);
        stage.setStartDate(project.getStartDate());
        stage.setEndDate(project.getEndDate());
        stage.setStatus(ProjectStageStatusType.PLANNED);
        stage.setOrderNumber(orderNumber);

        projectStageRepository.save(stage);
    }

    private void createFirstSprint(Project  project) {
        Sprint sprint = new Sprint();

        LocalDate sprintStartDate = project.getStartDate();
        LocalDate sprintEndDate = sprintStartDate.plusDays(13);

        if (project.getEndDate() != null && project.getEndDate().isBefore(sprintEndDate)) {
            sprintEndDate = project.getEndDate();
        }

        sprint.setProject(project);
        sprint.setName("Спринт 1");
        sprint.setStartDate(sprintStartDate);
        sprint.setEndDate(sprintEndDate);
        sprint.setGoal("Первый спринт проекта");

        sprintRepository.save(sprint);
    }

    @Override
    public Optional<Project> findById(Integer id) {
        return projectRepository.findById(id);
    }

    @Override
    public List<ProjectStage> findStagesByProjectId(Integer projectId) {
        return projectStageRepository.findByProjectIdOrderByOrderNumberAsc(projectId);
    }

    @Override
    public List<Sprint> findSprintsByProjectId(Integer projectId) {
        return sprintRepository.findByProjectIdOrderByStartDateAsc(projectId);
    }

    @Override
    public List<ProjectMember> findMembersByProjectId(Integer projectId) {
        return projectMemberRepository.findMembersByProjectId(projectId);
    }
}
