package backendlab.team4you.booking;

import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    private String id;

    private String name;

    private String email;

    private String phone;

    private LocalDateTime bookingTime;

    private LocalDateTime createdAt;

    

    @Enumerated(EnumType.STRING)
    private BookingEnum status;

    public BookingEntity() {}




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BookingEnum getStatus() {
        return status;
    }

    public void setStatus(BookingEnum status) {
        this.status = status;
    }

}