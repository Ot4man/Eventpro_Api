package com.eventpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RefundRequest(
        @NotNull(message = "L'identifiant de paiement est obligatoire")
        Long paymentId,

        @NotBlank(message = "Le motif du remboursement est obligatoire")
        String reason
) {
}

