package com.eventpro.controller;

import com.eventpro.entity.Promotion;
import com.eventpro.service.PromotionService;
import org.springframework.http.HttpStatus;
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
    //get promo by id
    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id);
    }
    //get promo by code
    @GetMapping("code/{code}")
    public Promotion getPromotionByCode(@PathVariable String code) {
        return promotionService.getPromotionByCode(code);
    }
    //get promo by eventid
    @GetMapping("/event/{eventId}")
    public List<Promotion> getPromotionByEvent(@PathVariable Long eventId) {return promotionService.getPromotionsByEvent(eventId);}

        //Create promotion
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion createPromotion(@RequestBody Promotion promotion) {
        return promotionService.createPromotion(promotion);
    }

    //Update promo
    @PutMapping("{id}")
    public Promotion updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
        return promotionService.updatePromotion(id, promotion);
    }

    //Delete promo
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long id){
        promotionService.deletePromotion(id);
    }

}