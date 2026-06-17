package com.example.EventBooking.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;
import com.example.EventBooking.model.*;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>{
    
}
