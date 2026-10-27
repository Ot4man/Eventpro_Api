package com.eventpro.mapper;

import com.eventpro.dto.PromotionResponseDTO;
import com.eventpro.entity.Promotion;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public PromotionResponseDTO toDTO(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        return new PromotionResponseDTO(
                promotion.getId(),
                promotion.getCode(),
                promotion.getDiscountType() != null ? promotion.getDiscountType().name() : null,
                promotion.getDiscountValue(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getUsageCount(),
                promotion.getUsageLimit(),
                promotion.getEvent() != null
                        ? promotion.getEvent().getId()
                        : null
        );
    }
}