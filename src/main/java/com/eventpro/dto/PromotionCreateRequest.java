package com.eventpro.dto;

import com.eventpro.entity.enums.DiscountType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PromotionCreateRequest(
        @NotBlank(message = "Le code promotionnel est obligatoire")
        String code,

        @NotNull(message = "L'identifiant de l'événement est obligatoire")
        Long eventId,

        @NotNull(message = "Le type de réduction est obligatoire")
        DiscountType discountType,

        @NotNull(message = "La valeur de réduction est obligatoire")
        @Positive(message = "La valeur de réduction doit être positive")
        BigDecimal discountValue,

        @NotNull(message = "La date de début est obligatoire")
        Instant startDate,

        @NotNull(message = "La date de fin est obligatoire")
        @Future(message = "La date de fin doit être dans le futur")
        Instant endDate,

        @Positive(message = "La limite d'utilisation doit être positive")
        Integer usageLimit
) {
}

