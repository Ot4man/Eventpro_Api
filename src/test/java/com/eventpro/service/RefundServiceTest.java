package com.eventpro.service;

import com.eventpro.dto.RefundRequest;
import com.eventpro.dto.RefundResponse;
import com.eventpro.entity.*;
import com.eventpro.entity.enums.PaymentStatus;
import com.eventpro.entity.enums.RefundStatus;
import com.eventpro.entity.enums.ReservationStatus;
import com.eventpro.entity.enums.TicketStatus;
import com.eventpro.repository.PaymentRepository;
import com.eventpro.repository.RefundRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;
import com.eventpro.repository.TicketRepository;
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
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCategoriesPerEventRepository categoryRepository;

    @InjectMocks
    private RefundService refundService;

    private Payment payment;
    private Reservation reservation;
    private Event event;
    private TicketCategoriesPerEvent eventCategory;
    private ReservationLine line;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .title("Tech Conference")
                .startDate(Instant.now().plus(72, ChronoUnit.HOURS))
                .build();

        eventCategory = TicketCategoriesPerEvent.builder()
                .id(10L)
                .event(event)
                .soldQuantity(2)
                .build();

        reservation = Reservation.builder()
                .id(100L)
                .status(ReservationStatus.CONFIRMED)
                .build();

        line = ReservationLine.builder()
                .id(50L)
                .reservation(reservation)
                .category(eventCategory)
                .quantity(2)
                .build();

        reservation.setLines(List.of(line));

        payment = Payment.builder()
                .id(500L)
                .reservation(reservation)
                .amount(new BigDecimal("400.00"))
                .status(PaymentStatus.SUCCESS)
                .build();

        ticket = Ticket.builder()
                .id(1L)
                .code("TKT-12345")
                .reservationLine(line)
                .status(TicketStatus.VALID)
                .build();
    }

    @Test
    @DisplayName("US5 & Règle 48h : Demande >= 48h avant l'événement -> 100% Remboursé, billets CANCELED, stock restitué")
    void testRefund_MoreThan48Hours_Approved() {
        RefundRequest request = RefundRequest.builder()
                .paymentId(500L)
                .reason("Empêchement professionnel")
                .build();

        when(paymentRepository.findById(500L)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByReservationLineReservationId(100L)).thenReturn(List.of(ticket));
        when(categoryRepository.findByIdWithLock(10L)).thenReturn(Optional.of(eventCategory));

        RefundResponse response = refundService.requestRefund(request);

        assertNotNull(response);
        assertEquals(RefundStatus.APPROVED, response.status());
        assertEquals(new BigDecimal("400.00"), response.amount());
        assertEquals(ReservationStatus.REFUNDED, reservation.getStatus());
        assertEquals(TicketStatus.CANCELED, ticket.getStatus());
        assertEquals(0, eventCategory.getSoldQuantity());
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    @DisplayName("US5 & Règle 48h : Demande < 48h avant l'événement -> Refus catégorique (0%), billets maintenus VALID")
    void testRefund_LessThan48Hours_Rejected() {
        event.setStartDate(Instant.now().plus(24, ChronoUnit.HOURS));

        RefundRequest request = RefundRequest.builder()
                .paymentId(500L)
                .reason("Changement d'avis tardif")
                .build();

        when(paymentRepository.findById(500L)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundResponse response = refundService.requestRefund(request);

        assertNotNull(response);
        assertEquals(RefundStatus.REJECTED, response.status());
        assertEquals(BigDecimal.ZERO, response.amount());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(TicketStatus.VALID, ticket.getStatus());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
