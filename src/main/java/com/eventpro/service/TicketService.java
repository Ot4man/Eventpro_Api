package com.eventpro.service;

import com.eventpro.dto.TicketResponse;
import com.eventpro.entity.Reservation;
import com.eventpro.entity.ReservationLine;
import com.eventpro.entity.Ticket;
import com.eventpro.entity.enums.TicketStatus;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.exception.TicketAlreadyUsedException;
import com.eventpro.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public List<Ticket> generateTicketsForReservation(Reservation reservation) {
        List<Ticket> generatedTickets = new ArrayList<>();

        for (ReservationLine line : reservation.getLines()) {
            for (int i = 0; i < line.getQuantity(); i++) {
                String uniqueCode = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Ticket ticket = Ticket.builder()
                        .code(uniqueCode)
                        .reservationLine(line)
                        .issueDate(Instant.now())
                        .status(TicketStatus.VALID)
                        .build();

                generatedTickets.add(ticketRepository.save(ticket));
            }
        }

        return generatedTickets;
    }

    @Transactional
    public TicketResponse getTicketByCode(String code) {
        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Billet introuvable avec le code : " + code));
        return toResponse(ticket);
    }

    @Transactional
    public List<TicketResponse> getTicketsByReservation(Long reservationId) {
        return ticketRepository.findByReservationLineReservationId(reservationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse scanTicket(String code) {
        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Billet introuvable avec le code : " + code));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new TicketAlreadyUsedException("Le billet présenté a déjà été scanné à l'entrée le : " + ticket.getScanDate());
        }

        if (ticket.getStatus() == TicketStatus.CANCELED) {
            throw new IllegalArgumentException("Ce billet a été annulé et n'est plus valable");
        }

        ticket.scan();
        Ticket updated = ticketRepository.save(ticket);
        return toResponse(updated);
    }

    public TicketResponse toResponse(Ticket ticket) {
        ReservationLine line = ticket.getReservationLine();
        return TicketResponse.builder()
                .id(ticket.getId())
                .code(ticket.getCode())
                .reservationId(line.getReservation().getId())
                .eventTitle(line.getCategory().getEvent().getTitle())
                .categoryName(line.getCategory().getCategory().getName())
                .price(line.getFixedUnitPrice())
                .clientName(line.getReservation().getClient().getNom())
                .status(ticket.getStatus())
                .issueDate(ticket.getIssueDate())
                .scanDate(ticket.getScanDate())
                .build();
    }
}
