package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query("""
            select pm.project
            from ProjectMember pm
            where pm.user.username = :username
            order by pm.project.createdAt desc
            """)
    List<Project> findProjectsByMemberUsername(@Param("username") String username);

    List<Project> findTop6ByPublicProjectTrueOrderByCreatedAtDesc();

    List<Project> findByPublicProjectTrueOrderByCreatedAtDesc();

    Optional<Project> findByIdAndPublicProjectTrue(Integer id);

    @Query("""
            select p
            from Project p
            where p.publicProject = true
              and lower(p.name) like lower(concat('%', :query, '%'))
            order by p.createdAt desc
            """)
    List<Project> searchPublicProjectsByName(@Param("query") String query);
}
