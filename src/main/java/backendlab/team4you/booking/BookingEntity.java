package backendlab.team4you.booking;

import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String reference;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "booking_time")
    private ZonedDateTime bookingTime;



    

    @Enumerated(EnumType.STRING)
    private BookingEnum status;

    public BookingEntity() {}




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public ZonedDateTime getBookingTime() {
        return bookingTime;
    }
    public void setBookingTime(ZonedDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }


    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }



    public BookingEnum getStatus() {
        return status;
    }

    public void setStatus(BookingEnum status) {
        this.status = status;
    }

}