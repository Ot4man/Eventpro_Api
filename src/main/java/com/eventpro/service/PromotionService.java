
package com.eventpro.service;

import com.eventpro.entity.Promotion;
import com.eventpro.exception.ResourceNotFoundException;
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

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }
    //get all promo
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    //find promo by id
    @Transactional(readOnly = true)
    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promotion introuvable avec l'id : " + id
                ));
    }
    //find promo by code
    @Transactional(readOnly = true)
    public  Promotion getPromotionByCode(String code){
        return promotionRepository.findByCodeIgnoreCase(code).orElseThrow(()-> new ResourceNotFoundException(
                "promtition not found by code:"+code
        ));
    }
    //find by Event
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsByEvent(Long eventId){
        return promotionRepository.findByEventId(eventId);
    }
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code,BigDecimal amount){
        if(amount == null || amount.signum() <0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        Promotion promotion = getPromotionByCode(code);
        if (!promotion.isValid()) {
            throw new IllegalArgumentException(
                    "promo not valide"
            );
        }

        return promotion.calculateDiscount(amount);
    }
    public void deletePromotion(Long id) {
        Promotion promotion = getPromotionById(id);
        promotionRepository.delete(promotion);
    }

    private void validateDates(Promotion promotion) {
        LocalDate start = promotion.getStartDate();
        LocalDate end = promotion.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "start and end date cannot be null"
            );
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "end date cannot be before start date"
            );
        }

        if (promotion.getDiscountValue() == null
                || promotion.getDiscountValue().signum() <= 0) {
            throw new IllegalArgumentException(
                    "value must be positive"
            );
        }
    }




    public Promotion createPromotion(Promotion promotion) {
        promotion.setId(null);
    if(promotion.getCode() == null || promotion.getCode().isBlank()){
        throw new IllegalArgumentException("Promotion code obligatory");

    }
    if(promotionRepository.existsByCodeIgnoreCase(promotion.getCode())){
        throw new IllegalArgumentException("the Code already exists");
    }
        validateDates(promotion);
        return promotionRepository.save(promotion);

    }
    public Promotion updatePromotion(Long id ,Promotion updatedPromotion) {
        Promotion existing = getPromotionById(id);
        existing.setCode(updatedPromotion.getCode());
        existing.setDiscountType(updatedPromotion.getDiscountType());
        existing.setDiscountValue(updatedPromotion.getDiscountValue());
        existing.setStartDate(updatedPromotion.getStartDate());
        existing.setEndDate(updatedPromotion.getEndDate());
        existing.setUsageLimit(updatedPromotion.getUsageLimit());
        validateDates(existing);
        return promotionRepository.save(existing);

    }
}
