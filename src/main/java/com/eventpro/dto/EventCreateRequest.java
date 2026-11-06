package com.eventpro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record EventCreateRequest(

        @NotBlank(message = "Title is mandatory")
        @Size(max = 150)
        String title,

        String description,

        @NotBlank(message = "Location is mandatory")
        @Size(max = 255)
        String location,

        @NotNull(message = "Start date is mandatory")
        Instant startDate,

        @NotNull(message = "End date is mandatory")
        Instant endDate,

        @NotNull(message = "Sales opening date is mandatory")
        Instant salesOpeningDate,

        @NotNull(message = "Sales closing date is mandatory")
        Instant salesClosingDate,

        @NotNull(message = "Capacity is mandatory")
        @Positive(message = "Capacity must be positive")
        Integer capacity,

        @NotNull(message = "Organizer ID is mandatory")
        @Positive(message = "Organizer ID must be positive")
        Long organizerId,

        @NotNull(message = "Categories are mandatory")
        @Size(min = 1, message = "At least one category is required")
        List<@Valid TicketCategoryRequest> categories

) {}
