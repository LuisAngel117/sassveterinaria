package com.sassveterinaria.clinical.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "soap_template")
public class SoapTemplateEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID branchId;

    @Column(nullable = false)
    private UUID serviceId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = true, columnDefinition = "text")
    private String sReason;

    @Column(nullable = true, columnDefinition = "text")
    private String sAnamnesis;

    @Column(nullable = true, columnDefinition = "text")
    private String oFindings;

    @Column(nullable = true, columnDefinition = "text")
    private String aDiagnosis;

    @Column(nullable = true, columnDefinition = "text")
    private String pTreatment;

    @Column(nullable = true, columnDefinition = "text")
    private String pInstructions;

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

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
