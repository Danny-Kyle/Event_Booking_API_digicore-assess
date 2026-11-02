package com.example.EventBooking.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;
import com.example.EventBooking.model.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
