package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.ProjectMember;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;
import ru.viktoria.projectteamworkorganizer.entity.id.ProjectMemberId;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    @Query("""
            select distinct pm from ProjectMember pm
            join fetch pm.user
            left join fetch pm.roles
            where pm.project.id = :projectId
            order by pm.joinedAt asc
            """)
    List<ProjectMember> findMembersByProjectId(@Param("projectId") Integer projectId);

    @Query("""
            select count(pm) from ProjectMember pm
            join pm.roles role
            where pm.project.id = :projectId
              and pm.user.username = :username
              and role.type = :roleType
            """)
    long countMemberRole(@Param("projectId") Integer projectId,
                         @Param("username") String username,
                         @Param("roleType") RoleType roleType);

    @Query("""
            select count(pm) from ProjectMember pm
            where pm.project.id = :projectId
              and pm.user.username = :username
            """)
    long countProjectMember(@Param("projectId") Integer projectId,
                            @Param("username") String username);
}