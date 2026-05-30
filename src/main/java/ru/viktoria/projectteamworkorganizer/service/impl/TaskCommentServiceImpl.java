package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.TaskCommentCreateDto;
import ru.viktoria.projectteamworkorganizer.entity.Task;
import ru.viktoria.projectteamworkorganizer.entity.TaskComment;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.repository.TaskCommentRepository;
import ru.viktoria.projectteamworkorganizer.repository.TaskRepository;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;
import ru.viktoria.projectteamworkorganizer.service.ProjectService;
import ru.viktoria.projectteamworkorganizer.service.TaskCommentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public TaskCommentServiceImpl(TaskCommentRepository taskCommentRepository,
                                  TaskRepository taskRepository,
                                  UserRepository userRepository,
                                  ProjectService projectService) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    @Override
    public List<TaskComment> findCommentsByTaskId(Integer taskId) {
        return taskCommentRepository.findCommentsByTaskId(taskId);
    }

    @Override
    @Transactional
    public void addComment(Integer taskId,
                           TaskCommentCreateDto commentCreateDto,
                           String currentUsername) {
        Optional<Task> taskOptional = taskRepository.findTaskDetailsById(taskId);

        if (taskOptional.isEmpty()) {
            throw new IllegalStateException("Задача не найдена");
        }

        Task task = taskOptional.get();
        Integer projectId = task.getProject().getId();

        if (!projectService.isProjectMember(projectId, currentUsername)) {
            throw new IllegalStateException("Нет доступа к задаче");
        }

        Optional<User> authorOptional = userRepository.findByUsername(currentUsername);

        if (authorOptional.isEmpty()) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }

        TaskComment comment = new TaskComment();

        comment.setTask(task);
        comment.setAuthor(authorOptional.get());
        comment.setCommentType(commentCreateDto.getCommentType());
        comment.setText(commentCreateDto.getText());
        comment.setCreatedAt(LocalDateTime.now());

        taskCommentRepository.save(comment);
    }
}
