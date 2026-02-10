package com.sassveterinaria.appointment.service;

import com.sassveterinaria.appointment.domain.RoomEntity;
import com.sassveterinaria.appointment.dto.RoomCreateRequest;
import com.sassveterinaria.appointment.dto.RoomResponse;
import com.sassveterinaria.appointment.repo.RoomRepository;
import com.sassveterinaria.security.AuthPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomResponse create(AuthPrincipal principal, RoomCreateRequest request) {
        RoomEntity entity = new RoomEntity();
        entity.setId(UUID.randomUUID());
        entity.setBranchId(principal.getBranchId());
        entity.setName(request.name().trim());
        entity.setActive(true);
        entity.setCreatedAt(OffsetDateTime.now());
        return toResponse(roomRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> list(AuthPrincipal principal) {
        return roomRepository
            .findByBranchIdAndIsActiveTrueOrderByNameAsc(principal.getBranchId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private RoomResponse toResponse(RoomEntity entity) {
        return new RoomResponse(
            entity.getId(),
            entity.getBranchId(),
            entity.getName(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
}
