package ru.viktoria.projectteamworkorganizer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.viktoria.projectteamworkorganizer.entity.enums.ActionObjectType;
import ru.viktoria.projectteamworkorganizer.entity.enums.UserActionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_action_logs")
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action_type", nullable = false, columnDefinition = "user_action_type")
    private UserActionType actionType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "object_type", nullable = false, columnDefinition = "action_object_type")
    private ActionObjectType objectType;

    @Column(name = "object_id")
    private Integer objectId;

    @Column(name = "object_name", length = 150)
    private String objectName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getActorUser() {
        return actorUser;
    }

    public void setActorUser(User actorUser) {
        this.actorUser = actorUser;
    }

    public UserActionType getActionType() {
        return actionType;
    }

    public void setActionType(UserActionType actionType) {
        this.actionType = actionType;
    }

    public ActionObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ActionObjectType objectType) {
        this.objectType = objectType;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
