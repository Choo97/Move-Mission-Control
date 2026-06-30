package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean published = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Review() {
    }

    public Review(Reservation reservation, Integer rating, String content) {
        this.reservation = reservation;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void publish() {
        this.published = true;
    }

    public void hide() {
        this.published = false;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public boolean isPublished() {
        return published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
