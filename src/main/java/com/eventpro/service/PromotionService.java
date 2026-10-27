
package com.eventpro.service;

import com.eventpro.entity.Event;
import com.eventpro.entity.Promotion;
import com.eventpro.entity.enums.DiscountType;
import com.eventpro.exception.BadRequestException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.EventRepository;
import com.eventpro.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final EventRepository eventRepository;

    public PromotionService(PromotionRepository promotionRepository, EventRepository eventRepository) {
        this.promotionRepository = promotionRepository;
        this.eventRepository = eventRepository;
    }

    // get all promo
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    // find promo by id
    @Transactional(readOnly = true)
    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promotion not found with id: " + id
                ));
    }

    // find promo by code
    @Transactional(readOnly = true)
    public Promotion getPromotionByCode(String code) {
        return promotionRepository.findByCodeIgnoreCase(code).orElseThrow(() -> new ResourceNotFoundException(
                "Promotion not found with code: " + code
        ));
    }

    // find by Event
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsByEvent(Long eventId) {
        return promotionRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }
        Promotion promotion = getPromotionByCode(code);
        if (!promotion.isValid()) {
            throw new BadRequestException("Promotion is not valid");
        }

        return promotion.calculateDiscount(amount);
    }

    public void deletePromotion(Long id) {
        Promotion promotion = getPromotionById(id);
        promotionRepository.delete(promotion);
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion.getCode() == null || promotion.getCode().isBlank()) {
            throw new BadRequestException("Promotion code is required");
        }

        if (promotion.getDiscountType() == null) {
            throw new BadRequestException("Discount type is required");
        }

        if (promotion.getDiscountValue() == null || promotion.getDiscountValue().signum() <= 0) {
            throw new BadRequestException("Discount value must be positive");
        }

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE
                && promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100%");
        }

        LocalDate start = promotion.getStartDate();
        LocalDate end = promotion.getEndDate();

        if (start == null || end == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        if (end.isBefore(start)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (promotion.getUsageLimit() <= 0) {
            throw new BadRequestException("Usage limit must be greater than 0");
        }
    }

    public Promotion createPromotion(Promotion promotion) {
        promotion.setId(null);
        validatePromotion(promotion);

        if (promotionRepository.existsByCodeIgnoreCase(promotion.getCode())) {
            throw new BadRequestException("The code already exists");
        }

        if (promotion.getEvent() == null || promotion.getEvent().getId() == null) {
            throw new BadRequestException("Event is required for promotion");
        }

        Event event = eventRepository.findById(promotion.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + promotion.getEvent().getId()
                ));
        promotion.setEvent(event);

        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long id, Promotion updatedPromotion) {
        Promotion existing = getPromotionById(id);
        validatePromotion(updatedPromotion);

        if (promotionRepository.existsByCodeIgnoreCaseAndIdNot(updatedPromotion.getCode(), id)) {
            throw new BadRequestException("The code already exists");
        }

        if (updatedPromotion.getUsageLimit() < existing.getUsageCount()) {
            throw new BadRequestException(
                    "Usage limit cannot be less than current usage count (" + existing.getUsageCount() + ")"
            );
        }

        if (updatedPromotion.getEvent() != null && updatedPromotion.getEvent().getId() != null) {
            Event event = eventRepository.findById(updatedPromotion.getEvent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Event not found with id: " + updatedPromotion.getEvent().getId()
                    ));
            existing.setEvent(event);
        }

        existing.setCode(updatedPromotion.getCode());
        existing.setDiscountType(updatedPromotion.getDiscountType());
        existing.setDiscountValue(updatedPromotion.getDiscountValue());
        existing.setStartDate(updatedPromotion.getStartDate());
        existing.setEndDate(updatedPromotion.getEndDate());
        existing.setUsageLimit(updatedPromotion.getUsageLimit());

        return promotionRepository.save(existing);
    }
}
