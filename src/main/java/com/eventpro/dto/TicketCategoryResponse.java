
package com.eventpro.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record TicketCategoryResponse(
        Long id,
        Long categoryId,
        String name,
        BigDecimal unitPrice,
        int totalQuantity,
        int maxQuotaPerClient
) {}
