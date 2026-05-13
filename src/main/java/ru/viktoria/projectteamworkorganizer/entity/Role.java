package ru.viktoria.projectteamworkorganizer.entity;

import jakarta.persistence.*;
import ru.viktoria.projectteamworkorganizer.entity.enums.RoleType;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = true, nullable = false, length = 50)
    private RoleType type;

    @Column(name = "description",  nullable = false)
    private String description;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setType(RoleType type) {
        this.type = type;
    }

    public RoleType getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
