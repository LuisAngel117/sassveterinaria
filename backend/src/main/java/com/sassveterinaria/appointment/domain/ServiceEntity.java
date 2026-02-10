package com.sassveterinaria.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "service")
public class ServiceEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID branchId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceBase;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPriceBase() {
        return priceBase;
    }

    public void setPriceBase(BigDecimal priceBase) {
        this.priceBase = priceBase;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
