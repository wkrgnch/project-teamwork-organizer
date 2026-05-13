package ru.viktoria.projectteamworkorganizer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.viktoria.projectteamworkorganizer.entity.enums.ProjectStageStatusType;

import java.time.LocalDate;

@Entity
@Table(name = "project_stages")
public class ProjectStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "project_stage_status_type")
    private ProjectStageStatusType status;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    public Project getProject() {
        return project;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public ProjectStageStatusType getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setStatus(ProjectStageStatusType status) {
        this.status = status;
    }
}
