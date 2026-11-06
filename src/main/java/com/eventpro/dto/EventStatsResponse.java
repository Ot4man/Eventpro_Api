package com.eventpro.dto;

import com.eventpro.entity.enums.EventStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record EventStatsResponse(
        Long eventId,
        String title,
        EventStatus status,
        int capacity,
        int totalTicketsSold,
        int totalTicketsAvailable,
        BigDecimal revenueGenerated
) {
}

