
package com.eventpro.service;

import com.eventpro.entity.Promotion;
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
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Promotion not found with id : " + id
                        )
                );
    }
    //find promo by code
    @Transactional(readOnly = true)
    public  Promotion getPromotionByCode(String code){
        return promotionRepository.findByCodeIgnoreCase(code).orElseThrow(()-> new IllegalArgumentException(
                "promtition not foun by code:"+code
        ));
    }
    //find by
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
