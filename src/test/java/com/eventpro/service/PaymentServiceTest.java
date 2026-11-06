package com.eventpro.service;

import com.eventpro.dto.PaymentRequest;
import com.eventpro.dto.PaymentResponse;
import com.eventpro.entity.*;
import com.eventpro.entity.enums.PaymentStatus;
import com.eventpro.entity.enums.ReservationStatus;
import com.eventpro.exception.IdempotencyConflictException;
import com.eventpro.exception.ReservationExpiredException;
import com.eventpro.repository.PaymentRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketCategoriesPerEventRepository categoryRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private PaymentService paymentService;

    private Reservation reservation;
    private TicketCategoriesPerEvent eventCategory;
    private ReservationLine line;

    @BeforeEach
    void setUp() {
        eventCategory = TicketCategoriesPerEvent.builder()
                .id(1L)
                .totalQuantity(100)
                .reservedQuantity(2)
                .soldQuantity(0)
                .build();

        reservation = Reservation.builder()
                .id(10L)
                .totalAmount(new BigDecimal("200.00"))
                .status(ReservationStatus.PENDING_PAYMENT)
                .expirationDate(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        line = ReservationLine.builder()
                .id(100L)
                .reservation(reservation)
                .category(eventCategory)
                .quantity(2)
                .fixedUnitPrice(new BigDecimal("100.00"))
                .build();

        reservation.setLines(List.of(line));
    }

    @Test
    @DisplayName("US4 : Paiement réussi -> Réservation confirmée, stock mis à jour, billets générés")
    void testProcessPayment_Success() {
        PaymentRequest request = PaymentRequest.builder()
                .reservationId(10L)
                .idempotencyKey("KEY-12345")
                .amount(new BigDecimal("200.00"))
                .build();

        when(paymentRepository.findByIdempotencyKey("KEY-12345")).thenReturn(Optional.empty());
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(categoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(eventCategory));

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(0, eventCategory.getReservedQuantity());
        assertEquals(2, eventCategory.getSoldQuantity());
        verify(ticketService, times(1)).generateTicketsForReservation(reservation);
    }

    @Test
    @DisplayName("Idempotence : Même clé avec réservation différente -> IdempotencyConflictException (HTTP 409)")
    void testProcessPayment_IdempotencyConflict() {
        Payment existingPayment = Payment.builder()
                .id(50L)
                .reservation(Reservation.builder().id(999L).build())
                .idempotencyKey("KEY-12345")
                .amount(new BigDecimal("200.00"))
                .build();

        when(paymentRepository.findByIdempotencyKey("KEY-12345")).thenReturn(Optional.of(existingPayment));

        PaymentRequest request = PaymentRequest.builder()
                .reservationId(10L)
                .idempotencyKey("KEY-12345")
                .amount(new BigDecimal("200.00"))
                .build();

        assertThrows(IdempotencyConflictException.class, () -> paymentService.processPayment(request));
    }

    @Test
    @DisplayName("Expiration : Paiement sur réservation expirée (>10min) -> ReservationExpiredException et libération du stock")
    void testProcessPayment_ExpiredReservation() {
        reservation.setExpirationDate(Instant.now().minus(5, ChronoUnit.MINUTES));

        when(paymentRepository.findByIdempotencyKey("KEY-EXPIRED")).thenReturn(Optional.empty());
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(categoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(eventCategory));

        PaymentRequest request = PaymentRequest.builder()
                .reservationId(10L)
                .idempotencyKey("KEY-EXPIRED")
                .amount(new BigDecimal("200.00"))
                .build();

        assertThrows(ReservationExpiredException.class, () -> paymentService.processPayment(request));
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertEquals(0, eventCategory.getReservedQuantity());
    }
}

