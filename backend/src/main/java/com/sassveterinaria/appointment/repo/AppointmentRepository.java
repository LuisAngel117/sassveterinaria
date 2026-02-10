package com.sassveterinaria.appointment.repo;

import com.sassveterinaria.appointment.domain.AppointmentEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findByBranchIdOrderByStartsAtAsc(UUID branchId);

    List<AppointmentEntity> findByBranchIdAndStartsAtGreaterThanEqualAndEndsAtLessThanEqualOrderByStartsAtAsc(
        UUID branchId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
    );
}
