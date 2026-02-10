package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.dto.ServiceResponse;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> list(AuthPrincipal principal) {
        return serviceRepository
            .findByBranchIdAndIsActiveTrueOrderByNameAsc(principal.getBranchId())
            .stream()
            .map(entity -> new ServiceResponse(
                entity.getId(),
                entity.getBranchId(),
                entity.getName(),
                entity.getDurationMinutes()
            ))
            .toList();
    }
}
