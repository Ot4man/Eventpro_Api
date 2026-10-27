package com.eventpro.controller;

import com.eventpro.dto.PromotionResponseDTO;
import com.eventpro.entity.Promotion;
import com.eventpro.mapper.PromotionMapper;
import com.eventpro.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public PromotionController(PromotionService promotionService, PromotionMapper promotionMapper) {
        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @GetMapping
    public List<PromotionResponseDTO> getAllPromotions() {
        return promotionService.getAllPromotions()
                .stream().map(promotionMapper::toDTO).toList();
    }
    //get promo by id
    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id);
    }
    //get promo by code
    @GetMapping("code/{code}")
    public PromotionResponseDTO getPromotionByCode(@PathVariable String code) {
        return promotionMapper.toDTO(promotionService.getPromotionByCode(code));
    }
    //get promo by eventid
    @GetMapping("/event/{eventId}")
    public List<PromotionResponseDTO> getPromotionByEvent(@PathVariable Long eventId) {return promotionService.getPromotionsByEvent(eventId).stream().map(promotionMapper::toDTO).toList();}

        //Create promotion
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponseDTO createPromotion(@RequestBody Promotion promotion) {
        return promotionMapper.toDTO(promotionService.createPromotion(promotion));
    }

    //Update promo
    @PutMapping("{id}")
    public PromotionResponseDTO updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
        return promotionMapper.toDTO(promotionService.updatePromotion(id,promotion));
    }

    //Delete promo
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long id){
        promotionService.deletePromotion(id);
    }

}