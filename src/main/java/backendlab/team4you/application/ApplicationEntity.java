package backendlab.team4you.application;

import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "application_entity")
public class ApplicationEntity {
    @Id
    private Long id;



    private String name;
    private String email;
    private String phone;
    private String message;
    private String title;
    private String description;

    public ApplicationEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }




    public String getEmail() {
        return email;
    }

    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private UserEntity owner;

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

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public String getMessage() {
        return message;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setMessage(String message) {
        this.message = message;
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
