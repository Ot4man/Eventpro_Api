package com.eventpro.service;

import com.eventpro.dto.ReservationCreateRequest;
import com.eventpro.dto.ReservationLineRequest;
import com.eventpro.dto.ReservationResponse;
import com.eventpro.entity.Client;
import com.eventpro.entity.Promotion;
import com.eventpro.entity.Reservation;
import com.eventpro.entity.ReservationLine;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.ReservationStatus;
import com.eventpro.exception.InsufficientStockException;
import com.eventpro.exception.ReservationExpiredException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.ClientRepository;
import com.eventpro.repository.PromotionRepository;
import com.eventpro.repository.ReservationRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final PromotionRepository promotionRepository;
    private final TicketCategoriesPerEventRepository categoryRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ClientRepository clientRepository,
            PromotionRepository promotionRepository,
            TicketCategoriesPerEventRepository categoryRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.promotionRepository = promotionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ReservationResponse createReservation(
            ReservationCreateRequest request
    ) {
        if (request == null
                || request.clientId() == null
                || request.lines() == null
                || request.lines().isEmpty()) {
            throw new IllegalArgumentException(
                    "Les données de réservation sont obligatoires"
            );
        }

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client introuvable : " + request.clientId()
                ));

        Map<Long, Integer> quantities = new HashMap<>();

        for (ReservationLineRequest line : request.lines()) {
            if (line == null || line.categoryId() == null
                    || line.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Catégorie et quantité valides obligatoires"
                );
            }

            quantities.merge(
                    line.categoryId(),
                    line.quantity(),
                    Integer::sum
            );
        }

        List<ReservationLine> reservationLines = new ArrayList<>();
        Long eventId = null;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            TicketCategoriesPerEvent category =
                    categoryRepository.findByIdWithLock(entry.getKey())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Catégorie de billets introuvable : "
                                            + entry.getKey()
                                    ));

            int quantity = entry.getValue();

            if (!category.checkStock(quantity)) {
                throw new InsufficientStockException(
                        "Il n'y a plus assez de places libres pour valider la réservation"
                );
            }

            if (quantity > category.getMaxQuotaPerClient()) {
                throw new IllegalArgumentException(
                        "Quota maximum dépassé pour la catégorie : "
                                + category.getId()
                                + " (maximum autorisé : " + category.getMaxQuotaPerClient() + ")"
                );
            }

            category.reservePlaces(quantity);

            Long currentEventId = category.getEvent().getId();

            if (eventId != null && !eventId.equals(currentEventId)) {
                throw new IllegalArgumentException(
                        "Une réservation doit concerner un seul événement"
                );
            }

            eventId = currentEventId;

            ReservationLine line = ReservationLine.builder()
                    .category(category)
                    .quantity(quantity)
                    .fixedUnitPrice(category.getUnitPrice())
                    .build();

            reservationLines.add(line);
        }

        Promotion promotion = null;

        if (request.promotionCode() != null
                && !request.promotionCode().isBlank()) {

            promotion = promotionRepository
                    .findByCodeIgnoreCase(request.promotionCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Code promotionnel introuvable : " + request.promotionCode()
                    ));

            if (!promotion.isValid()) {
                throw new IllegalArgumentException(
                        "Cette promotion n'est pas valide"
                );
            }

            if (!promotion.getEvent().getId().equals(eventId)) {
                throw new IllegalArgumentException(
                        "Cette promotion ne concerne pas cet événement"
                );
            }
        }

        Instant now = Instant.now();

        Reservation reservation = Reservation.builder()
                .reference("RES-" + UUID.randomUUID())
                .client(client)
                .creationDate(now)
                .expirationDate(now.plus(10, ChronoUnit.MINUTES))
                .status(ReservationStatus.PENDING_PAYMENT)
                .promotion(promotion)
                .lines(reservationLines)
                .build();

        for (ReservationLine line : reservationLines) {
            line.setReservation(reservation);
        }

        reservation.setTotalAmount(reservation.calculateTotal());

        if (promotion != null) {
            promotion.incrementUsage();
        }

        Reservation saved = reservationRepository.save(reservation);

        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable : " + id
                ));

        return toResponse(reservation);
    }

    @Transactional
    public List<ReservationResponse> getReservationsByClient(Long clientId) {
        return reservationRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse confirmReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable : " + id
                ));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException(
                    "Seule une réservation en attente de paiement peut être confirmée"
            );
        }

        if (!Instant.now().isBefore(reservation.getExpirationDate())) {
            reservation.expire();

            for (ReservationLine line : reservation.getLines()) {
                categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .ifPresent(cat -> cat.releaseReservedPlaces(line.getQuantity()));
            }

            reservationRepository.save(reservation);

            throw new ReservationExpiredException(
                    "Le paiement arrive trop tard : les 10 minutes sont déjà écoulées"
            );
        }

        for (ReservationLine line : reservation.getLines()) {
            TicketCategoriesPerEvent cat = categoryRepository.findByIdWithLock(line.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
            cat.confirmPlaces(line.getQuantity());
        }

        reservation.confirm();

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable : " + id
                ));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Cette réservation ne peut pas être annulée"
            );
        }

        if (reservation.getStatus() == ReservationStatus.PENDING_PAYMENT) {
            for (ReservationLine line : reservation.getLines()) {
                categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .ifPresent(cat -> cat.releaseReservedPlaces(line.getQuantity()));
            }
        }

        reservation.cancel();

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public int expirePendingReservations() {
        Instant now = Instant.now();
        List<Reservation> expiredReservations =
                reservationRepository.findByStatusAndExpirationDateBefore(
                        ReservationStatus.PENDING_PAYMENT,
                        now
                );

        for (Reservation reservation : expiredReservations) {
            reservation.expire();

            for (ReservationLine line : reservation.getLines()) {
                categoryRepository.findByIdWithLock(line.getCategory().getId())
                        .ifPresent(cat -> cat.releaseReservedPlaces(line.getQuantity()));
            }
        }

        return expiredReservations.size();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReference(),
                reservation.getClient().getId(),
                reservation.getCreationDate(),
                reservation.getExpirationDate(),
                reservation.getStatus(),
                reservation.getTotalAmount()
        );
    }
}
