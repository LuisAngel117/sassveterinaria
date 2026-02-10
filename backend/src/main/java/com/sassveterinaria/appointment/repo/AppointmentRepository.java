package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.AppointmentEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    Optional<AppointmentEntity> findByIdAndBranchId(UUID id, UUID branchId);

    @Query("""
        SELECT a
        FROM AppointmentEntity a
        WHERE a.branchId = :branchId
          AND (:from IS NULL OR :to IS NULL OR (a.startsAt < :to AND a.endsAt > :from))
          AND (:roomId IS NULL OR a.roomId = :roomId)
          AND (:status IS NULL OR a.status = :status)
        ORDER BY a.startsAt ASC
        """)
    List<AppointmentEntity> search(
        @Param("branchId") UUID branchId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("roomId") UUID roomId,
        @Param("status") String status
    );

    @Query("""
        SELECT (COUNT(a) > 0)
        FROM AppointmentEntity a
        WHERE a.branchId = :branchId
          AND a.roomId = :roomId
          AND a.status <> 'CANCELLED'
          AND (:excludeAppointmentId IS NULL OR a.id <> :excludeAppointmentId)
          AND :startsAt < a.endsAt
          AND :endsAt > a.startsAt
        """)
    boolean existsConflictInRoom(
        @Param("branchId") UUID branchId,
        @Param("roomId") UUID roomId,
        @Param("startsAt") OffsetDateTime startsAt,
        @Param("endsAt") OffsetDateTime endsAt,
        @Param("excludeAppointmentId") UUID excludeAppointmentId
    );
}
