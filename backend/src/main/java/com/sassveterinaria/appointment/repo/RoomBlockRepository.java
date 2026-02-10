package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.RoomBlockEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomBlockRepository extends JpaRepository<RoomBlockEntity, UUID> {
    Optional<RoomBlockEntity> findByIdAndBranchId(UUID id, UUID branchId);

    @Query("""
        SELECT (COUNT(rb) > 0)
        FROM RoomBlockEntity rb
        WHERE rb.branchId = :branchId
          AND rb.roomId = :roomId
          AND :startsAt < rb.endsAt
          AND :endsAt > rb.startsAt
        """)
    boolean existsConflictInRoom(
        @Param("branchId") UUID branchId,
        @Param("roomId") UUID roomId,
        @Param("startsAt") OffsetDateTime startsAt,
        @Param("endsAt") OffsetDateTime endsAt
    );

    @Query("""
        SELECT rb
        FROM RoomBlockEntity rb
        WHERE rb.branchId = :branchId
          AND rb.startsAt < :to
          AND rb.endsAt > :from
          AND (:roomId IS NULL OR rb.roomId = :roomId)
        ORDER BY rb.startsAt ASC
        """)
    List<RoomBlockEntity> findInRange(
        @Param("branchId") UUID branchId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("roomId") UUID roomId
    );
}
