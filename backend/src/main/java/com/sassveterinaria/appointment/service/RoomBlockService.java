package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.domain.RoomBlockEntity;
import com.sassveterinaria.appointment.dto.RoomBlockCreateRequest;
import com.sassveterinaria.appointment.dto.RoomBlockResponse;
import com.sassveterinaria.appointment.repo.RoomBlockRepository;
import com.sassveterinaria.appointment.repo.RoomRepository;
import com.sassveterinaria.common.ApiProblemException;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomBlockService {

    private final RoomBlockRepository roomBlockRepository;
    private final RoomRepository roomRepository;

    public RoomBlockService(RoomBlockRepository roomBlockRepository, RoomRepository roomRepository) {
        this.roomBlockRepository = roomBlockRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomBlockResponse create(AuthPrincipal principal, RoomBlockCreateRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/invalid-time-range",
                "Invalid time range",
                "endsAt debe ser mayor que startsAt.",
                "INVALID_TIME_RANGE"
            );
        }

        roomRepository.findByIdAndBranchIdAndIsActiveTrue(request.roomId(), principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/room-not-found",
                "Room not found",
                "La sala no existe o esta inactiva.",
                "ROOM_NOT_FOUND"
            ));

        RoomBlockEntity entity = new RoomBlockEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setRoomId(request.roomId());
        entity.setStartsAt(request.startsAt());
        entity.setEndsAt(request.endsAt());
        entity.setReason(request.reason().trim());
        entity.setCreatedBy(principal.getUserId());
        entity.setCreatedAt(OffsetDateTime.now());
        return toResponse(roomBlockRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<RoomBlockResponse> list(AuthPrincipal principal, OffsetDateTime from, OffsetDateTime to, UUID roomId) {
        if (from == null || to == null) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/invalid-time-range",
                "Invalid time range",
                "Debes enviar from y to.",
                "INVALID_TIME_RANGE"
            );
        }
        if (!to.isAfter(from)) {
            throw new ApiProblemException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "https://sassveterinaria.local/errors/invalid-time-range",
                "Invalid time range",
                "to debe ser mayor que from.",
                "INVALID_TIME_RANGE"
            );
        }

        return roomBlockRepository
            .findInRange(principal.getBranchId(), from, to, roomId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void delete(AuthPrincipal principal, UUID roomBlockId) {
        RoomBlockEntity entity = roomBlockRepository.findByIdAndBranchId(roomBlockId, principal.getBranchId())
            .orElseThrow(() -> new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/room-block-not-found",
                "Room block not found",
                "No se encontro el bloqueo de agenda.",
                "ROOM_BLOCK_NOT_FOUND"
            ));
        roomBlockRepository.delete(entity);
    }

    private RoomBlockResponse toResponse(RoomBlockEntity entity) {
        return new RoomBlockResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getRoomId(),
            entity.getStartsAt(),
            entity.getEndsAt(),
            entity.getReason(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
