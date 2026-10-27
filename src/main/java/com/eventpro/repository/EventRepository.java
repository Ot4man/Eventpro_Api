
package com.eventpro.repository;

import com.eventpro.entity.Event;
import com.eventpro.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByTitleContainingIgnoreCase(String title);
}
