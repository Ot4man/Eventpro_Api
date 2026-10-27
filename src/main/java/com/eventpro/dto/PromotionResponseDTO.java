package com.eventpro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionResponseDTO(
        Long id,
        String code,
        String discountType,
        BigDecimal discountValue,
        LocalDate startDate,
        LocalDate endDate,
        Integer usageCount,
        Integer usageLimit,
        Long eventId
) {}