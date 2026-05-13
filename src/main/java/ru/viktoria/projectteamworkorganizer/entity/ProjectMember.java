package ru.viktoria.projectteamworkorganizer.entity;


import jakarta.persistence.*;
import ru.viktoria.projectteamworkorganizer.entity.id.ProjectMemberId;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id = new ProjectMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "team_role", length = 100)
    private String teamRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public Project getProject() {
        return project;
    }

    public ProjectMemberId getId() {
        return id;
    }

    public String getTeamRole() {
        return teamRole;
    }

    public User getUser() {
        return user;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setId(ProjectMemberId id) {
        this.id = id;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public void setTeamRole(String teamRole) {
        this.teamRole = teamRole;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
