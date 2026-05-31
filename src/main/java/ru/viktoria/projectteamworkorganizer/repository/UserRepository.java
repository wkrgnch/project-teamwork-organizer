package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktoria.projectteamworkorganizer.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "systemRole")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
