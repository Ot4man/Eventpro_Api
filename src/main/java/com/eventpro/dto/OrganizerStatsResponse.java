package com.eventpro.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrganizerStatsResponse(
        Long organizerId,
        String organizerName,
        int totalEvents,
        int totalTicketsSold,
        BigDecimal totalRevenue,
        List<EventStatsResponse> eventStats
) {
}

