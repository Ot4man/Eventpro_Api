package com.eventpro.service;

import com.eventpro.dto.PaymentRequest;
import com.eventpro.dto.PaymentResponse;
import com.eventpro.entity.Payment;
import com.eventpro.entity.Reservation;
import com.eventpro.entity.ReservationLine;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.PaymentStatus;
import com.eventpro.entity.enums.ReservationStatus;
import com.eventpro.exception.IdempotencyConflictException;
import com.eventpro.exception.ReservationExpiredException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.PaymentRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketCategoriesPerEventRepository categoryRepository;
    private final TicketService ticketService;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            TicketCategoriesPerEventRepository categoryRepository,
            TicketService ticketService
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.categoryRepository = categoryRepository;
        this.ticketService = ticketService;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête de paiement est obligatoire");
        }

        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingPayment.isPresent()) {
            Payment prev = existingPayment.get();
            if (!prev.getReservation().getId().equals(request.reservationId())
                    || prev.getAmount().compareTo(request.amount()) != 0) {
                throw new IdempotencyConflictException("Cette clé d'idempotence a déjà été utilisée pour un panier différent");
            }
            return toResponse(prev);
        }

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + request.reservationId()));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Seule une réservation en attente de paiement peut être payée");
        }

        if (!Instant.now().isBefore(reservation.getExpirationDate())) {
            reservation.expire();
            for (ReservationLine line : reservation.getLines()) {
                categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .ifPresent(cat -> cat.releaseReservedPlaces(line.getQuantity()));
            }
            reservationRepository.save(reservation);
            throw new ReservationExpiredException("Le délai de 10 minutes pour régler la réservation est dépassé");
        }

        if (request.amount().compareTo(reservation.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Le montant envoyé (" + request.amount()
                    + ") ne correspond pas au total de la réservation (" + reservation.getTotalAmount() + ")");
        }

        boolean isFailure = Boolean.TRUE.equals(request.simulateFailure());

        Payment payment = Payment.builder()
                .reservation(reservation)
                .idempotencyKey(request.idempotencyKey())
                .reference("PAY-" + UUID.randomUUID())
                .amount(request.amount())
                .paymentDate(Instant.now())
                .status(isFailure ? PaymentStatus.FAILED : PaymentStatus.SUCCESS)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (!isFailure) {
            reservation.confirm();

            for (ReservationLine line : reservation.getLines()) {
                TicketCategoriesPerEvent cat = categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
                cat.confirmPlaces(line.getQuantity());
            }

            reservationRepository.save(reservation);

            ticketService.generateTicketsForReservation(reservation);
        }

        return toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));
        return toResponse(payment);
    }

    @Transactional
    public List<PaymentResponse> getPaymentsByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reference(payment.getReference())
                .reservationId(payment.getReservation().getId())
                .idempotencyKey(payment.getIdempotencyKey())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .build();
    }
}
