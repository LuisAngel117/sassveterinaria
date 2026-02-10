package com.sassveterinaria.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_branch")
public class UserBranchEntity {

    @EmbeddedId
    private UserBranchId id;

    @Column(nullable = false)
    private boolean isDefault;

    public UserBranchId getId() {
        return id;
    }

    public void setId(UserBranchId id) {
        this.id = id;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
