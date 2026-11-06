package com.eventpro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ReservationLineRequest(
        @NotNull(message = "L'identifiant de catégorie est obligatoire")
        Long categoryId,

        @Positive(message = "La quantité doit être strictement positive")
        int quantity
) {
}
