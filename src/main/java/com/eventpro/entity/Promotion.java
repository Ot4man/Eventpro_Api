
package com.eventpro.entity;

import com.eventpro.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int usageLimit;

    @Column(nullable = false)
    private int usageCount;

    public boolean isValid() {
        LocalDate today = LocalDate.now();

        return today.compareTo(startDate) >= 0
                && today.compareTo(endDate) <= 0
                && usageCount < usageLimit;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || !isValid()) {
            return BigDecimal.ZERO;
        }

        if (discountType == DiscountType.PERCENTAGE) {
            return amount.multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }

        if (discountType == DiscountType.FIXED_AMOUNT) {
            return discountValue.min(amount);
        }

        return BigDecimal.ZERO;
    }
}
