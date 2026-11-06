package com.eventpro.entity;

import com.eventpro.entity.enums.DiscountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {

    @Test
    void testIsValid_WhenDatesAreNull_ShouldReturnFalseWithoutException() {
        Promotion promo = Promotion.builder()
                .code("NULL_DATES")
                .usageLimit(10)
                .usageCount(0)
                .build();

        assertFalse(promo.isValid());
    }

    @Test
    void testIsValid_WhenActive_ShouldReturnTrue() {
        Promotion promo = Promotion.builder()
                .code("ACTIVE")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(5)
                .usageCount(2)
                .build();

        assertTrue(promo.isValid());
    }

    @Test
    void testIsValid_WhenUsageLimitReached_ShouldReturnFalse() {
        Promotion promo = Promotion.builder()
                .code("MAXED")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(5)
                .usageCount(5)
                .build();

        assertFalse(promo.isValid());
    }

    @Test
    void testCalculateDiscount_Percentage() {
        Promotion promo = Promotion.builder()
                .code("DISC20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(10)
                .usageCount(0)
                .build();

        BigDecimal discount = promo.calculateDiscount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("20.00"), discount);
    }

    @Test
    void testCalculateDiscount_PercentageCappedAtAmount() {
        Promotion promo = Promotion.builder()
                .code("DISC150")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("150.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(10)
                .usageCount(0)
                .build();

        BigDecimal discount = promo.calculateDiscount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), discount);
    }

    @Test
    void testCalculateDiscount_FixedAmount() {
        Promotion promo = Promotion.builder()
                .code("FIXED30")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("30.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(10)
                .usageCount(0)
                .build();

        BigDecimal discount = promo.calculateDiscount(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("30.00"), discount);
    }

    @Test
    void testReservationCalculateTotal_WithValidPromotion() {
        Promotion promo = Promotion.builder()
                .code("SAVE20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .usageLimit(10)
                .usageCount(0)
                .build();

        ReservationLine line = ReservationLine.builder()
                .quantity(2)
                .fixedUnitPrice(new BigDecimal("50.00"))
                .build();

        Reservation reservation = Reservation.builder()
                .lines(List.of(line))
                .promotion(promo)
                .build();

        // subtotal = 100.00, discount = 20.00, total = 80.00
        BigDecimal total = reservation.calculateTotal();
        assertEquals(new BigDecimal("80.00"), total);
    }
}
