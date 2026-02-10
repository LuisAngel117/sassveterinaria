package com.sassveterinaria.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
    @NotBlank @Size(max = 160) String fullName,
    @Size(max = 30) String identification,
    @Size(max = 30) String phone,
    @Email @Size(max = 160) String email,
    @Size(max = 255) String address,
    String notes
) {
}
