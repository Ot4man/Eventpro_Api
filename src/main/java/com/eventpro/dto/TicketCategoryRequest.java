
package com.eventpro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TicketCategoryRequest(

        @NotNull(message = "Category ID is mandatory")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @NotNull(message = "Unit price is mandatory")
        @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        @NotNull(message = "Total quantity is mandatory")
        @Positive(message = "Total quantity must be positive")
        Integer totalQuantity,

        @NotNull(message = "Maximum quota per client is mandatory")
        @Positive(message = "Maximum quota per client must be positive")
        Integer maxQuotaPerClient

) {}
