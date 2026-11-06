package com.eventpro.dto;
import com.eventpro.entity.enums.ReservationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ReservationResponse(
        Long id,
        String reference,
        Long clientId,
        Instant creationDate,
        Instant expirationDate,
        ReservationStatus status,
        BigDecimal totalAmount
) {
}
