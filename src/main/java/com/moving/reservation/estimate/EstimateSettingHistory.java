package com.moving.reservation.estimate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class EstimateSettingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_setting_id", nullable = false)
    private EstimateSetting estimateSetting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstimateSettingKey settingKey;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false)
    private int previousAmount;

    @Column(nullable = false)
    private int changedAmount;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 50)
    private String changedBy;

    protected EstimateSettingHistory() {
    }

    public EstimateSettingHistory(EstimateSetting estimateSetting, int previousAmount, int changedAmount, String changedBy) {
        this.estimateSetting = estimateSetting;
        this.settingKey = estimateSetting.getSettingKey();
        this.label = estimateSetting.getLabel();
        this.previousAmount = previousAmount;
        this.changedAmount = changedAmount;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public EstimateSetting getEstimateSetting() {
        return estimateSetting;
    }

    public EstimateSettingKey getSettingKey() {
        return settingKey;
    }

    public String getLabel() {
        return label;
    }

    public int getPreviousAmount() {
        return previousAmount;
    }

    public int getChangedAmount() {
        return changedAmount;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
