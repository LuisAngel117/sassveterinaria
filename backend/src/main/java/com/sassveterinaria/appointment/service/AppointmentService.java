package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.domain.AppointmentEntity;
import com.sassveterinaria.appointment.dto.AppointmentCreateRequest;
import com.sassveterinaria.appointment.dto.AppointmentResponse;
import com.sassveterinaria.appointment.repo.AppointmentRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public AppointmentResponse create(AuthPrincipal principal, AppointmentCreateRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/invalid-time-range",
                "Invalid time range",
                "endsAt debe ser mayor que startsAt.",
                "INVALID_TIME_RANGE"
            );
        }

        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setStartsAt(request.startsAt());
        entity.setEndsAt(request.endsAt());
        entity.setStatus(request.status().toUpperCase());
        entity.setReason(request.reason());
        entity.setNotes(request.notes());
        entity.setRoomId(request.roomId());
        entity.setClientId(request.clientId());
        entity.setPetId(request.petId());
        entity.setVeterinarianId(request.veterinarianId());
        entity.setCreatedAt(OffsetDateTime.now());

        AppointmentEntity saved = appointmentRepository.save(entity);
        return toResponse(saved);
    }

    public List<AppointmentResponse> list(AuthPrincipal principal, OffsetDateTime from, OffsetDateTime to) {
        List<AppointmentEntity> items;
        if (from != null && to != null) {
            items = appointmentRepository
                .findByBranchIdAndStartsAtGreaterThanEqualAndEndsAtLessThanEqualOrderByStartsAtAsc(principal.getBranchId(), from, to);
        } else {
            items = appointmentRepository.findByBranchIdOrderByStartsAtAsc(principal.getBranchId());
        }
        return items.stream().map(this::toResponse).toList();
    }

    private AppointmentResponse toResponse(AppointmentEntity entity) {
        return new AppointmentResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getStartsAt(),
            entity.getEndsAt(),
            entity.getStatus(),
            entity.getReason(),
            entity.getNotes(),
            entity.getRoomId(),
            entity.getClientId(),
            entity.getPetId(),
            entity.getVeterinarianId(),
            entity.getCreatedAt()
        );
    }
}
