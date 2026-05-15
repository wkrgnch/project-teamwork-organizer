package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
            select pm.project
            from ProjectMember pm
            where pm.user.username = :username
            order by pm.project.createdAt desc
            """)
    List<Project> findProjectsByMemberUsername(@Param("username") String username);
}
