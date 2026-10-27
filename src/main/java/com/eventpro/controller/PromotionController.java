package com.eventpro.controller;

import com.eventpro.entity.Promotion;
import com.eventpro.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotions();
    }

    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id);
    }

    @GetMapping("code/{code}")
    public Promotion getPromotionById(@PathVariable String code) {
        return promotionService.getPromotionByCode(code);
    }

}