package com.eventpro.service;

import com.eventpro.dto.EventStatsResponse;
import com.eventpro.dto.OrganizerStatsResponse;
import com.eventpro.entity.Event;
import com.eventpro.entity.Organizer;
import com.eventpro.entity.Payment;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.PaymentStatus;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.EventRepository;
import com.eventpro.repository.OrganizerRepository;
import com.eventpro.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizerStatsService {

    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;

    public OrganizerStatsService(
            OrganizerRepository organizerRepository,
            EventRepository eventRepository,
            PaymentRepository paymentRepository
    ) {
        this.organizerRepository = organizerRepository;
        this.eventRepository = eventRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public EventStatsResponse getEventStats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + eventId));

        int totalTicketsSold = event.getCategories().stream()
                .mapToInt(TicketCategoriesPerEvent::getSoldQuantity)
                .sum();

        int totalTicketsAvailable = event.getCategories().stream()
                .mapToInt(TicketCategoriesPerEvent::getAvailableQuantity)
                .sum();

        BigDecimal revenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS
                        && !p.getReservation().getLines().isEmpty()
                        && p.getReservation().getLines().get(0).getCategory().getEvent().getId().equals(eventId))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EventStatsResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .status(event.getStatus())
                .capacity(event.getCapacity())
                .totalTicketsSold(totalTicketsSold)
                .totalTicketsAvailable(totalTicketsAvailable)
                .revenueGenerated(revenue)
                .build();
    }

    @Transactional
    public OrganizerStatsResponse getOrganizerStats(Long organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisateur introuvable : " + organizerId));

        List<Event> events = eventRepository.findByOrganizerId(organizerId);

        List<EventStatsResponse> eventStatsList = new ArrayList<>();
        int totalTicketsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Event event : events) {
            EventStatsResponse stats = getEventStats(event.getId());
            eventStatsList.add(stats);
            totalTicketsSold += stats.totalTicketsSold();
            totalRevenue = totalRevenue.add(stats.revenueGenerated());
        }

        return OrganizerStatsResponse.builder()
                .organizerId(organizer.getId())
                .organizerName(organizer.getNomOrganisation() != null ? organizer.getNomOrganisation() : organizer.getNom())
                .totalEvents(events.size())
                .totalTicketsSold(totalTicketsSold)
                .totalRevenue(totalRevenue)
                .eventStats(eventStatsList)
                .build();
    }
}
