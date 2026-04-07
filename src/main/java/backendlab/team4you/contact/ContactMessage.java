package backendlab.team4you.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDateTime;



@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    private Long id;

    private String firstName;
    private String email;

    private String phone;
    private String lastName;


    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime submittedAt = LocalDateTime.now();


    public ContactMessage() {
    }
    public ContactMessage(String firstName, String email, String phone, String lastName, String message) {
        this.firstName = firstName;
        this.email = email;
        this.phone = phone;
        this.lastName = lastName;
        this.message = message;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;

    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
