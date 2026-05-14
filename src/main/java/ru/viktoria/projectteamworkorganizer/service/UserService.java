package ru.viktoria.projectteamworkorganizer.service;

import ru.viktoria.projectteamworkorganizer.dto.UserRegisterDto;
import ru.viktoria.projectteamworkorganizer.entity.User;

public interface UserService {
    User register(UserRegisterDto userRegisterDto);

    boolean existsByUsername(String username);
}
