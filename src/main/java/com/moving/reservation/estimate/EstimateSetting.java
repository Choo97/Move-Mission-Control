package com.moving.reservation.estimate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class EstimateSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private EstimateSettingKey settingKey;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int sortOrder;

    protected EstimateSetting() {
    }

    public EstimateSetting(EstimateSettingKey settingKey) {
        this.settingKey = settingKey;
        this.label = settingKey.getLabel();
        this.amount = settingKey.getDefaultAmount();
        this.sortOrder = settingKey.getSortOrder();
    }

    public void updateAmount(int amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public EstimateSettingKey getSettingKey() {
        return settingKey;
    }

    public String getLabel() {
        return label;
    }

    public int getAmount() {
        return amount;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
