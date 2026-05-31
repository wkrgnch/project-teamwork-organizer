package ru.viktoria.projectteamworkorganizer.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktoria.projectteamworkorganizer.dto.ChangePasswordDto;
import ru.viktoria.projectteamworkorganizer.dto.DeleteProfileDto;
import ru.viktoria.projectteamworkorganizer.entity.User;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;
import ru.viktoria.projectteamworkorganizer.repository.UserRepository;
import ru.viktoria.projectteamworkorganizer.service.UserActionLogService;
import ru.viktoria.projectteamworkorganizer.service.UserProfileService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserActionLogService userActionLogService;

    public UserProfileServiceImpl(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  UserActionLogService userActionLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userActionLogService = userActionLogService;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDto changePasswordDto) {
        User user = getActiveUser(username);

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Текущий пароль указан неверно");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new IllegalStateException("Новый пароль и подтверждение не совпадают");
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);

        userActionLogService.log(
                username,
                UserActionType.CHANGE_PASSWORD,
                ActionObjectType.USER,
                user.getId(),
                user.getUsername(),
                "Пользователь изменил пароль"
        );
    }

    @Override
    @Transactional
    public void deleteProfile(String username, DeleteProfileDto deleteProfileDto) {
        User user = getActiveUser(username);

        if (!passwordEncoder.matches(deleteProfileDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Текущий пароль указан неверно");
        }

        String deletedMarker = "deleted_user_" + user.getId();

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setUsername(deletedMarker);
        user.setEmail(deletedMarker + "@deleted.local");
        user.setFullName("Удаленный пользователь");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        userRepository.save(user);

        userActionLogService.log(
                username,
                UserActionType.UPDATE_PROFILE,
                ActionObjectType.USER,
                user.getId(),
                deletedMarker,
                "Пользователь удалил профиль"
        );
    }

    private User getActiveUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден");
        }

        User user = userOptional.get();

        if (user.isDeleted()) {
            throw new IllegalStateException("Профиль уже удалён");
        }

        return user;
    }
}
