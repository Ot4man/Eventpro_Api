
package com.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "reservation_lines")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReservationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_category_event_id", nullable = false)
    private TicketCategoriesPerEvent category;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fixedUnitPrice;

    public BigDecimal calculateSubtotal() {
        if (quantity <= 0 || fixedUnitPrice == null) {
            return BigDecimal.ZERO;
        }

        return fixedUnitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}
