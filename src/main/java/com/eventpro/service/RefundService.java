package com.eventpro.service;

import com.eventpro.dto.RefundRequest;
import com.eventpro.dto.RefundResponse;
import com.eventpro.entity.Event;
import com.eventpro.entity.Payment;
import com.eventpro.entity.Refund;
import com.eventpro.entity.Reservation;
import com.eventpro.entity.ReservationLine;
import com.eventpro.entity.Ticket;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.PaymentStatus;
import com.eventpro.entity.enums.RefundStatus;
import com.eventpro.entity.enums.ReservationStatus;
import com.eventpro.entity.enums.TicketStatus;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.PaymentRepository;
import com.eventpro.repository.RefundRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;
import com.eventpro.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final TicketCategoriesPerEventRepository categoryRepository;

    public RefundService(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            TicketRepository ticketRepository,
            TicketCategoriesPerEventRepository categoryRepository
    ) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public RefundResponse requestRefund(RefundRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête de remboursement est obligatoire");
        }

        Payment payment = paymentRepository.findById(request.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + request.paymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Seul un paiement réussi peut faire l'objet d'une demande de remboursement");
        }

        Reservation reservation = payment.getReservation();
        if (reservation.getLines().isEmpty()) {
            throw new IllegalArgumentException("La réservation associée ne contient aucune ligne");
        }

        Event event = reservation.getLines().get(0).getCategory().getEvent();
        Instant now = Instant.now();

        long hoursUntilEvent = Duration.between(now, event.getStartDate()).toHours();

        if (hoursUntilEvent >= 48) {
            Refund refund = Refund.builder()
                    .payment(payment)
                    .amount(payment.getAmount())
                    .reason(request.reason())
                    .requestDate(now)
                    .status(RefundStatus.APPROVED)
                    .build();

            Refund savedRefund = refundRepository.save(refund);

            List<Ticket> tickets = ticketRepository.findByReservationLineReservationId(reservation.getId());
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELED);
                ticketRepository.save(ticket);
            }

            for (ReservationLine line : reservation.getLines()) {
                categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .ifPresent(cat -> cat.releaseSoldPlaces(line.getQuantity()));
            }

            reservation.setStatus(ReservationStatus.REFUNDED);
            reservationRepository.save(reservation);

            return toResponse(savedRefund, "Remboursement intégral de 100% accordé.");
        } else {
            Refund refund = Refund.builder()
                    .payment(payment)
                    .amount(BigDecimal.ZERO)
                    .reason(request.reason())
                    .requestDate(now)
                    .status(RefundStatus.REJECTED)
                    .build();

            Refund savedRefund = refundRepository.save(refund);

            return toResponse(savedRefund, "Demande de remboursement refusée (délai inférieur à 48h).");
        }
    }

    @Transactional
    public void processAutomaticRefundForCanceledEvent(Long eventId) {
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> !r.getLines().isEmpty()
                        && r.getLines().get(0).getCategory().getEvent().getId().equals(eventId)
                        && r.getStatus() == ReservationStatus.CONFIRMED)
                .toList();

        for (Reservation reservation : reservations) {
            List<Payment> payments = paymentRepository.findByReservationId(reservation.getId());
            for (Payment payment : payments) {
                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                    Refund refund = Refund.builder()
                            .payment(payment)
                            .amount(payment.getAmount())
                            .reason("Annulation automatique de l'événement par l'organisateur")
                            .requestDate(Instant.now())
                            .status(RefundStatus.APPROVED)
                            .build();

                    refundRepository.save(refund);
                }
            }

            List<Ticket> tickets = ticketRepository.findByReservationLineReservationId(reservation.getId());
            for (Ticket ticket : tickets) {
                ticket.setStatus(TicketStatus.CANCELED);
                ticketRepository.save(ticket);
            }

            reservation.setStatus(ReservationStatus.REFUNDED);
            reservationRepository.save(reservation);
        }
    }

    @Transactional
    public RefundResponse getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remboursement introuvable : " + id));
        return toResponse(refund, null);
    }

    @Transactional
    public List<RefundResponse> getRefundsByPayment(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId).stream()
                .map(r -> toResponse(r, null))
                .toList();
    }

    private RefundResponse toResponse(Refund refund, String message) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment().getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .requestDate(refund.getRequestDate())
                .status(refund.getStatus())
                .message(message != null ? message : "Statut : " + refund.getStatus())
                .build();
    }
}
