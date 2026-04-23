package backendlab.team4you.application;

import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "application_entity")
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private UserEntity owner;

    private String title;
    private String description;



    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;



    public UserEntity getOwner() {
        return owner;
    }
    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    public void setUpdatedAt(ZonedDateTime now) {
        this.updatedAt = now;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(ZonedDateTime now) {
        this.createdAt = now;
    }
    public ApplicationStatus getStatus() {
        return status;
    }
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }




}
