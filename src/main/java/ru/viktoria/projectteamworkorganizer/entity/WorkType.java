package ru.viktoria.projectteamworkorganizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "work_types")
public class WorkType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type", unique = true, nullable = false, length = 100)
    private String type;

    @Column(name = "description",  nullable = false)
    private String description;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
