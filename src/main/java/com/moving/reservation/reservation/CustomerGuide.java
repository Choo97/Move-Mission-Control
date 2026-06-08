package com.moving.reservation.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class CustomerGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected CustomerGuide() {
    }

    public CustomerGuide(ReservationStatus status, String title, String description, Integer displayOrder) {
        this.status = status;
        this.title = title;
        this.description = description;
        this.displayOrder = displayOrder;
        this.createdAt = LocalDateTime.now();
    }

    public void update(ReservationStatus status, String title, String description, Integer displayOrder) {
        this.status = status;
        this.title = title;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public CustomerGuideItem toItem() {
        return new CustomerGuideItem(title, description);
    }

    public Long getId() {
        return id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
