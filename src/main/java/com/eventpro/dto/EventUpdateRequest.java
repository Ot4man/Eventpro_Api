
package com.eventpro.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record EventUpdateRequest(

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
        Integer capacity
) {}
