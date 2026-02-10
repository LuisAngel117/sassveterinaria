package com.sassveterinaria.clinical.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "visit")
public class VisitEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID branchId;

    @Column(nullable = false)
    private UUID petId;

    @Column(nullable = false)
    private UUID serviceId;

    @Column(nullable = true)
    private UUID appointmentId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, columnDefinition = "text")
    private String sReason;

    @Column(nullable = false, columnDefinition = "text")
    private String sAnamnesis;

    @Column(nullable = true, precision = 6, scale = 2)
    private BigDecimal oWeightKg;

    @Column(nullable = true, precision = 4, scale = 1)
    private BigDecimal oTemperatureC;

    @Column(nullable = true, columnDefinition = "text")
    private String oFindings;

    @Column(nullable = true, columnDefinition = "text")
    private String aDiagnosis;

    @Column(nullable = true, length = 30)
    private String aSeverity;

    @Column(nullable = true, columnDefinition = "text")
    private String pTreatment;

    @Column(nullable = true, columnDefinition = "text")
    private String pInstructions;

    @Column(nullable = true)
    private LocalDate pFollowupAt;

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

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

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSReason() {
        return sReason;
    }

    public void setSReason(String sReason) {
        this.sReason = sReason;
    }

    public String getSAnamnesis() {
        return sAnamnesis;
    }

    public void setSAnamnesis(String sAnamnesis) {
        this.sAnamnesis = sAnamnesis;
    }

    public BigDecimal getOWeightKg() {
        return oWeightKg;
    }

    public void setOWeightKg(BigDecimal oWeightKg) {
        this.oWeightKg = oWeightKg;
    }

    public BigDecimal getOTemperatureC() {
        return oTemperatureC;
    }

    public void setOTemperatureC(BigDecimal oTemperatureC) {
        this.oTemperatureC = oTemperatureC;
    }

    public String getOFindings() {
        return oFindings;
    }

    public void setOFindings(String oFindings) {
        this.oFindings = oFindings;
    }

    public String getADiagnosis() {
        return aDiagnosis;
    }

    public void setADiagnosis(String aDiagnosis) {
        this.aDiagnosis = aDiagnosis;
    }

    public String getASeverity() {
        return aSeverity;
    }

    public void setASeverity(String aSeverity) {
        this.aSeverity = aSeverity;
    }

    public String getPTreatment() {
        return pTreatment;
    }

    public void setPTreatment(String pTreatment) {
        this.pTreatment = pTreatment;
    }

    public String getPInstructions() {
        return pInstructions;
    }

    public void setPInstructions(String pInstructions) {
        this.pInstructions = pInstructions;
    }

    public LocalDate getPFollowupAt() {
        return pFollowupAt;
    }

    public void setPFollowupAt(LocalDate pFollowupAt) {
        this.pFollowupAt = pFollowupAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
