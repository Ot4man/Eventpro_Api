
package com.eventpro.dto;

import com.eventpro.entity.enums.EventStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record EventResponse(
        Long id,
        Long organizerId,
        String title,
        String description,
        String location,
        Instant startDate,
        Instant endDate,
        Instant salesOpeningDate,
        Instant salesClosingDate,
        EventStatus status,
        int capacity,
        List<TicketCategoryResponse> categories
) {}
