package com.eventpro.service;

import com.eventpro.entity.Event;
import com.eventpro.entity.Promotion;
import com.eventpro.entity.enums.DiscountType;
import com.eventpro.exception.BadRequestException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.EventRepository;
import com.eventpro.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion validPromotion;
    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder().id(1L).build();
        validPromotion = Promotion.builder()
                .id(1L)
                .code("PROMO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .usageCount(0)
                .event(event)
                .build();
    }

    @Test
    void createPromotion_Success() {
        when(promotionRepository.existsByCodeIgnoreCase("PROMO10")).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Promotion created = promotionService.createPromotion(validPromotion);
        assertNotNull(created);
        assertEquals("PROMO10", created.getCode());
        verify(promotionRepository).save(validPromotion);
    }

    @Test
    void createPromotion_ThrowsWhenCodeAlreadyExists() {
        when(promotionRepository.existsByCodeIgnoreCase("PROMO10")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.createPromotion(validPromotion));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void createPromotion_ThrowsWhenEventNotFound() {
        when(promotionRepository.existsByCodeIgnoreCase("PROMO10")).thenReturn(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                promotionService.createPromotion(validPromotion));
    }

    @Test
    void createPromotion_ThrowsWhenDiscountPercentageGreaterThan100() {
        validPromotion.setDiscountValue(new BigDecimal("150.00"));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.createPromotion(validPromotion));
        assertTrue(ex.getMessage().contains("cannot exceed 100%"));
    }

    @Test
    void updatePromotion_ThrowsWhenNewCodeAlreadyExistsOnAnotherPromo() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(validPromotion));
        when(promotionRepository.existsByCodeIgnoreCaseAndIdNot("TAKEN", 1L)).thenReturn(true);

        Promotion updateRequest = Promotion.builder()
                .code("TAKEN")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.updatePromotion(1L, updateRequest));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void updatePromotion_ThrowsWhenUsageLimitBelowUsageCount() {
        validPromotion.setUsageCount(15);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(validPromotion));
        when(promotionRepository.existsByCodeIgnoreCaseAndIdNot("PROMO10", 1L)).thenReturn(false);

        Promotion updateRequest = Promotion.builder()
                .code("PROMO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(10) // below 15
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.updatePromotion(1L, updateRequest));
        assertTrue(ex.getMessage().contains("Usage limit cannot be less than current usage count"));
    }
}


