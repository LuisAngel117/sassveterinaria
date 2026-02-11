package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.domain.AppointmentEntity;
import com.sassveterinaria.appointment.domain.AppointmentStatus;
import com.sassveterinaria.appointment.domain.ServiceEntity;
import com.sassveterinaria.appointment.dto.AppointmentCreateRequest;
import com.sassveterinaria.appointment.dto.AppointmentResponse;
import com.sassveterinaria.appointment.dto.AppointmentUpdateRequest;
import com.sassveterinaria.appointment.repo.AppointmentRepository;
import com.sassveterinaria.appointment.repo.RoomBlockRepository;
import com.sassveterinaria.appointment.repo.RoomRepository;
import com.sassveterinaria.appointment.repo.ServiceRepository;
import com.sassveterinaria.audit.service.AuditService;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final RoomBlockRepository roomBlockRepository;
    private final AuditService auditService;

    public AppointmentService(
        AppointmentRepository appointmentRepository,
        RoomRepository roomRepository,
        ServiceRepository serviceRepository,
        RoomBlockRepository roomBlockRepository,
        AuditService auditService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.roomBlockRepository = roomBlockRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AppointmentResponse create(AuthPrincipal principal, AppointmentCreateRequest request) {
        requireRoom(principal, request.roomId());
        ServiceEntity service = requireService(principal, request.serviceId());

        OffsetDateTime startsAt = request.startsAt();
        OffsetDateTime endsAt = startsAt.plusMinutes(service.getDurationMinutes());
        validateTimeRange(startsAt, endsAt);

        OverbookResult overbookResult = resolveOverlap(
            principal,
            request.roomId(),
            startsAt,
            endsAt,
            null,
            request.overbookReason()
        );

        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setRoomId(request.roomId());
        entity.setServiceId(request.serviceId());
        entity.setStartsAt(startsAt);
        entity.setEndsAt(endsAt);
        entity.setStatus(AppointmentStatus.RESERVED.name());
        entity.setReason(request.reason());
        entity.setNotes(request.notes());
        entity.setClientId(request.clientId());
        entity.setPetId(request.petId());
        entity.setVeterinarianId(request.veterinarianId());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setCheckedInAt(null);
        entity.setOverbook(overbookResult.isOverbook());
        entity.setOverbookReason(overbookResult.overbookReason());

        AppointmentEntity saved = appointmentRepository.save(entity);
        auditService.recordEvent(
            principal,
            "APPT_CREATE",
            "appointment",
            saved.getId(),
            buildSnapshot(saved)
        );

        if (saved.isOverbook()) {
            Map<String, Object> afterPayload = buildSnapshot(saved);
            afterPayload.put("overbookReason", saved.getOverbookReason());
            auditService.recordSensitiveEvent(
                principal,
                "APPT_OVERBOOK",
                "appointment",
                saved.getId(),
                saved.getOverbookReason(),
                Map.of("roomId", saved.getRoomId(), "startsAt", saved.getStartsAt(), "endsAt", saved.getEndsAt()),
                afterPayload
            );
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> list(
        AuthPrincipal principal,
        OffsetDateTime from,
        OffsetDateTime to,
        UUID roomId,
        String status
    ) {
        if ((from == null) != (to == null)) {
            throw invalidRange("Debes enviar from y to juntos.");
        }
        if (from != null && !to.isAfter(from)) {
            throw invalidRange("El parametro to debe ser mayor que from.");
        }

        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            AppointmentStatus parsedStatus = AppointmentStatus.fromValue(status);
            if (parsedStatus == null) {
                throw invalidTransition("status invalido.");
            }
            normalizedStatus = parsedStatus.name();
        }

        return appointmentRepository
            .search(principal.getBranchId(), from, to, roomId, normalizedStatus)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AppointmentResponse reschedule(AuthPrincipal principal, UUID appointmentId, AppointmentUpdateRequest request) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        if (isTerminalStatus(appointment)) {
            throw invalidTransition("No se puede reprogramar una cita cancelada o cerrada.");
        }

        requireRoom(principal, request.roomId());
        ServiceEntity service = requireService(principal, appointment.getServiceId());

        OffsetDateTime startsAt = request.startsAt();
        OffsetDateTime endsAt = startsAt.plusMinutes(service.getDurationMinutes());
        validateTimeRange(startsAt, endsAt);

        OverbookResult overbookResult = resolveOverlap(
            principal,
            request.roomId(),
            startsAt,
            endsAt,
            appointment.getId(),
            request.overbookReason()
        );

        Map<String, Object> beforePayload = buildSnapshot(appointment);
        appointment.setRoomId(request.roomId());
        appointment.setStartsAt(startsAt);
        appointment.setEndsAt(endsAt);
        appointment.setOverbook(overbookResult.isOverbook());
        appointment.setOverbookReason(overbookResult.overbookReason());

        AppointmentEntity saved = appointmentRepository.save(appointment);
        auditService.recordEvent(
            principal,
            "APPT_UPDATE",
            "appointment",
            saved.getId(),
            buildSnapshot(saved)
        );

        if (saved.isOverbook()) {
            auditService.recordSensitiveEvent(
                principal,
                "APPT_OVERBOOK",
                "appointment",
                saved.getId(),
                saved.getOverbookReason(),
                beforePayload,
                buildSnapshot(saved)
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse confirm(AuthPrincipal principal, UUID appointmentId) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        requireTransition(appointment, AppointmentStatus.RESERVED, AppointmentStatus.CONFIRMED);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        auditService.recordEvent(principal, "APPT_CONFIRM", "appointment", saved.getId(), buildSnapshot(saved));
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse start(AuthPrincipal principal, UUID appointmentId) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        requireTransition(appointment, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_ATTENTION);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        auditService.recordEvent(principal, "APPT_START", "appointment", saved.getId(), buildSnapshot(saved));
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse close(AuthPrincipal principal, UUID appointmentId) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        requireTransition(appointment, AppointmentStatus.IN_ATTENTION, AppointmentStatus.CLOSED);
        AppointmentEntity saved = appointmentRepository.save(appointment);
        auditService.recordEvent(principal, "APPT_CLOSE", "appointment", saved.getId(), buildSnapshot(saved));
        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancel(AuthPrincipal principal, UUID appointmentId, String reason) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        AppointmentStatus currentStatus = parseStatus(appointment.getStatus());

        if (currentStatus == AppointmentStatus.CANCELLED || currentStatus == AppointmentStatus.CLOSED) {
            throw invalidTransition("No se puede cancelar una cita en estado terminal.");
        }

        boolean sensitiveCancellation = currentStatus == AppointmentStatus.CONFIRMED || currentStatus == AppointmentStatus.IN_ATTENTION;
        String normalizedReason = sensitiveCancellation ? normalizeSensitiveReason(reason) : normalizeOptionalReason(reason);

        Map<String, Object> beforePayload = buildSnapshot(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED.name());
        appointment.setReason(normalizedReason);
        appointment.setOverbook(false);
        appointment.setOverbookReason(null);
        AppointmentEntity saved = appointmentRepository.save(appointment);

        if (sensitiveCancellation) {
            auditService.recordSensitiveEvent(
                principal,
                "APPT_CANCEL",
                "appointment",
                saved.getId(),
                normalizedReason,
                beforePayload,
                buildSnapshot(saved)
            );
        } else {
            auditService.recordEvent(
                principal,
                "APPT_CANCEL",
                "appointment",
                saved.getId(),
                buildSnapshot(saved)
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse checkin(AuthPrincipal principal, UUID appointmentId) {
        AppointmentEntity appointment = requireAppointment(principal, appointmentId);
        AppointmentStatus status = parseStatus(appointment.getStatus());
        if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.CLOSED) {
            throw invalidTransition("No se puede hacer check-in sobre una cita cancelada o cerrada.");
        }

        appointment.setCheckedInAt(OffsetDateTime.now());
        AppointmentEntity saved = appointmentRepository.save(appointment);
        auditService.recordEvent(principal, "APPT_CHECKIN", "appointment", saved.getId(), buildSnapshot(saved));
        return toResponse(saved);
    }

    private OverbookResult resolveOverlap(
        AuthPrincipal principal,
        UUID roomId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        UUID excludeAppointmentId,
        String overbookReason
    ) {
        boolean appointmentConflict = appointmentRepository.existsConflictInRoom(
            principal.getBranchId(),
            roomId,
            startsAt,
            endsAt,
            excludeAppointmentId
        );
        boolean roomBlockConflict = roomBlockRepository.existsConflictInRoom(
            principal.getBranchId(),
            roomId,
            startsAt,
            endsAt
        );

        if (!appointmentConflict && !roomBlockConflict) {
            return new OverbookResult(false, null);
        }

        if (!principal.getPermissions().contains("APPT_OVERBOOK")) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/appointment-overlap",
                "Appointment overlap",
                "Existe solape en la sala seleccionada.",
                "APPT_OVERLAP"
            );
        }

        return new OverbookResult(true, normalizeSensitiveReason(overbookReason));
    }

    private AppointmentEntity requireAppointment(AuthPrincipal principal, UUID appointmentId) {
        return appointmentRepository.findByIdAndBranchId(appointmentId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/appointment-not-found",
                "Appointment not found",
                "No se encontro la cita.",
                "APPT_NOT_FOUND"
            ));
    }

    private void requireRoom(AuthPrincipal principal, UUID roomId) {
        roomRepository.findByIdAndBranchIdAndIsActiveTrue(roomId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/room-not-found",
                "Room not found",
                "La sala no existe o esta inactiva.",
                "ROOM_NOT_FOUND"
            ));
    }

    private ServiceEntity requireService(AuthPrincipal principal, UUID serviceId) {
        return serviceRepository.findByIdAndBranchIdAndIsActiveTrue(serviceId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/service-not-found",
                "Service not found",
                "El servicio no existe o esta inactivo.",
                "SERVICE_NOT_FOUND"
            ));
    }

    private ApiProblemException invalidRange(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/invalid-time-range",
            "Invalid time range",
            detail,
            "INVALID_TIME_RANGE"
        );
    }

    private ApiProblemException invalidTransition(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/appointment-invalid-transition",
            "Invalid appointment transition",
            detail,
            "APPT_INVALID_TRANSITION"
        );
    }

    private ApiProblemException reasonRequired(String detail) {
        return new ApiProblemException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "https://sassveterinaria.local/errors/reason-required",
            "Reason required",
            detail,
            "REASON_REQUIRED"
        );
    }

    private void validateTimeRange(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw invalidRange("endsAt debe ser mayor que startsAt.");
        }
    }

    private void requireTransition(AppointmentEntity appointment, AppointmentStatus expectedCurrent, AppointmentStatus target) {
        AppointmentStatus current = parseStatus(appointment.getStatus());
        if (current != expectedCurrent) {
            throw invalidTransition(
                "Transicion invalida: " + current.name() + " -> " + target.name()
            );
        }
        appointment.setStatus(target.name());
    }

    private AppointmentStatus parseStatus(String rawStatus) {
        AppointmentStatus status = AppointmentStatus.fromValue(rawStatus);
        if (status == null) {
            throw invalidTransition("Estado actual desconocido: " + rawStatus);
        }
        return status;
    }

    private boolean isTerminalStatus(AppointmentEntity appointment) {
        AppointmentStatus status = parseStatus(appointment.getStatus());
        return status == AppointmentStatus.CANCELLED || status == AppointmentStatus.CLOSED;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptionalReason(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSensitiveReason(String value) {
        String normalized = normalizeOptionalReason(value);
        if (normalized == null || normalized.length() < 10) {
            throw reasonRequired("reason es requerido (minimo 10 caracteres).");
        }
        return normalized;
    }

    private Map<String, Object> buildSnapshot(AppointmentEntity appointment) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", appointment.getId());
        snapshot.put("branchId", appointment.getBranchId());
        snapshot.put("roomId", appointment.getRoomId());
        snapshot.put("serviceId", appointment.getServiceId());
        snapshot.put("startsAt", appointment.getStartsAt());
        snapshot.put("endsAt", appointment.getEndsAt());
        snapshot.put("status", appointment.getStatus());
        snapshot.put("checkedInAt", appointment.getCheckedInAt());
        snapshot.put("isOverbook", appointment.isOverbook());
        snapshot.put("overbookReason", appointment.getOverbookReason());
        snapshot.put("reason", appointment.getReason());
        return snapshot;
    }

    private AppointmentResponse toResponse(AppointmentEntity entity) {
        return new AppointmentResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getRoomId(),
            entity.getServiceId(),
            entity.getStartsAt(),
            entity.getEndsAt(),
            entity.getStatus(),
            entity.getCheckedInAt(),
            entity.isOverbook(),
            entity.getOverbookReason(),
            entity.getReason(),
            entity.getNotes(),
            entity.getClientId(),
            entity.getPetId(),
            entity.getVeterinarianId(),
            entity.getCreatedAt()
        );
    }

    private record OverbookResult(boolean isOverbook, String overbookReason) {
    }
}
