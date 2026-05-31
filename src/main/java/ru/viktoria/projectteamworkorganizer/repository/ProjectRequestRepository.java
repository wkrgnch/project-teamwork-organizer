package ru.viktoria.projectteamworkorganizer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktoria.projectteamworkorganizer.entity.ProjectRequest;
import ru.viktoria.projectteamworkorganizer.entity.enums.RequestStatusType;

import java.util.List;
import java.util.Optional;

public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Integer> {

    @EntityGraph(attributePaths = {"project", "author"})
    Optional<ProjectRequest> findDetailedById(Integer id);

    @Query("""
            select request from ProjectRequest request
            join fetch request.project
            left join fetch request.author
            where request.project.id = :projectId
              and request.status not in :excludedStatuses
            order by request.createdAt desc
            """)
    List<ProjectRequest> findRequestsByProjectIdExcludingStatuses(@Param("projectId") Integer projectId,
                                                                  @Param("excludedStatuses") List<RequestStatusType> excludedStatuses);

    @Query("""
            select request from ProjectRequest request
            join fetch request.project
            left join fetch request.author
            where request.project.id = :projectId
              and request.author.username = :username
            order by request.createdAt desc
            """)
    List<ProjectRequest> findRequestsByProjectIdAndAuthorUsername(@Param("projectId") Integer projectId,
                                                                  @Param("username") String username);
}
