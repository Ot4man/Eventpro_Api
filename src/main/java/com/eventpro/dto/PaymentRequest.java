package com.eventpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentRequest(
        @NotNull(message = "L'identifiant de réservation est obligatoire")
        Long reservationId,

        @NotBlank(message = "La clé d'idempotence est obligatoire")
        String idempotencyKey,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être strictement positif")
        BigDecimal amount,

        Boolean simulateFailure
) {
}

