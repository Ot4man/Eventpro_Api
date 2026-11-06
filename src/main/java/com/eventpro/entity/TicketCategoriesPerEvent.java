package com.eventpro.entity;

import com.eventpro.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_categories_per_event")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TicketCategoriesPerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private TicketCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private int soldQuantity = 0;

    @Column(nullable = false)
    private int maxQuotaPerClient;

    public int getAvailableQuantity() {
        return totalQuantity - (reservedQuantity + soldQuantity);
    }

    public boolean checkStock(int quantity) {
        return quantity > 0 && quantity <= getAvailableQuantity();
    }

    public void reservePlaces(int quantity) {
        if (!checkStock(quantity)) {
            throw new InsufficientStockException("Il n'y a plus assez de places libres pour valider la réservation");
        }
        this.reservedQuantity += quantity;
    }

    public void releaseReservedPlaces(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void confirmPlaces(int quantity) {
        if (this.reservedQuantity < quantity) {
            throw new IllegalArgumentException("Quantité réservée incohérente");
        }
        this.reservedQuantity -= quantity;
        this.soldQuantity += quantity;
    }

    public void releaseSoldPlaces(int quantity) {
        this.soldQuantity = Math.max(0, this.soldQuantity - quantity);
    }
}
