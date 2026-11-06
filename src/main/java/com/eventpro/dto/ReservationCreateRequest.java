package com.eventpro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record ReservationCreateRequest(
        @NotNull(message = "L'identifiant du client est obligatoire")
        Long clientId,

        @NotNull(message = "Les lignes de réservation sont obligatoires")
        @NotEmpty(message = "Au moins une ligne de réservation est requise")
        @Valid
        List<ReservationLineRequest> lines,

        String promotionCode
) {
}
