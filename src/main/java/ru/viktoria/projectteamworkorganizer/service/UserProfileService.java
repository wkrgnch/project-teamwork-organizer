package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.ChangePasswordDto;
import ru.viktoria.projectteamworkorganizer.dto.DeleteProfileDto;
import ru.viktoria.projectteamworkorganizer.entity.User;

import java.util.Optional;

public interface UserProfileService {

    Optional<User> findByUsername(String username);

    void changePassword(String username, ChangePasswordDto changePasswordDto);

    void deleteProfile(String username, DeleteProfileDto deleteProfileDto);
}
