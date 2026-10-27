package com.eventpro.repository;

import com.eventpro.entity.Promotion;
import com.eventpro.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    Optional<Promotion> findByCodeIgnoreCase(String code);
    List<Promotion> findByEventId(Long eventId);
    boolean existsByCodeIgnoreCase(String code);

}
