package com.eventpro.service;

import com.eventpro.dto.EventCreateRequest;
import com.eventpro.dto.EventResponse;
import com.eventpro.dto.EventUpdateRequest;
import com.eventpro.entity.Event;
import com.eventpro.entity.Organizer;
import com.eventpro.entity.TicketCategory;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.EventStatus;
import com.eventpro.exception.BadRequestException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.mapper.EventMapper;
import com.eventpro.repository.EventRepository;
import com.eventpro.repository.OrganizerRepository;
import com.eventpro.repository.TicketCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventMapper eventMapper;
    private final RefundService refundService;

    public EventService(
            EventRepository eventRepository,
            OrganizerRepository organizerRepository,
            TicketCategoryRepository ticketCategoryRepository,
            EventMapper eventMapper,
            RefundService refundService
    ) {
        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.eventMapper = eventMapper;
        this.refundService = refundService;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        return eventMapper.toResponse(findEventById(id));
    }

    public EventResponse createEvent(EventCreateRequest request) {

        if (!request.endDate().isAfter(request.startDate())) {
            throw new BadRequestException(
                    "End date must be after start date"
            );
        }

        if (!request.salesClosingDate().isAfter(request.salesOpeningDate())) {
            throw new BadRequestException(
                    "Sales closing date must be after sales opening date"
            );
        }

        if (request.salesOpeningDate().isAfter(request.startDate())) {
            throw new BadRequestException(
                    "Sales opening date must be before or equal to event start date"
            );
        }

        if (request.salesClosingDate().isAfter(request.startDate())) {
            throw new BadRequestException(
                    "Sales closing date must be before or equal to event start date"
            );
        }

        Organizer organizer = organizerRepository
                .findById(request.organizerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organizer not found: " + request.organizerId()
                        )
                );

        Event event = Event.builder()
                .organizer(organizer)
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .salesOpeningDate(request.salesOpeningDate())
                .salesClosingDate(request.salesClosingDate())
                .capacity(request.capacity())
                .status(EventStatus.DRAFT)
                .build();

        for (var categoryRequest : request.categories()) {

            TicketCategory category = ticketCategoryRepository
                    .findById(categoryRequest.categoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Ticket category not found: "
                                            + categoryRequest.categoryId()
                                )
                    );

            TicketCategoriesPerEvent configuration =
                    TicketCategoriesPerEvent.builder()
                            .event(event)
                            .category(category)
                            .unitPrice(categoryRequest.unitPrice())
                            .totalQuantity(categoryRequest.totalQuantity())
                            .maxQuotaPerClient(
                                    categoryRequest.maxQuotaPerClient()
                            )
                            .build();

            event.getCategories().add(configuration);
        }

        int totalQuantity = event.getCategories()
                .stream()
                .mapToInt(TicketCategoriesPerEvent::getTotalQuantity)
                .sum();

        if (totalQuantity > event.getCapacity()) {
            throw new BadRequestException(
                    "Total ticket quantity cannot exceed event capacity"
            );
        }

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        Event existingEvent = findEventById(id);

        if (!request.endDate().isAfter(request.startDate())) {
            throw new BadRequestException(
                    "End date must be after start date"
            );
        }

        existingEvent.setTitle(request.title());
        existingEvent.setDescription(request.description());
        existingEvent.setLocation(request.location());
        existingEvent.setStartDate(request.startDate());
        existingEvent.setEndDate(request.endDate());
        existingEvent.setSalesOpeningDate(request.salesOpeningDate());
        existingEvent.setSalesClosingDate(request.salesClosingDate());
        existingEvent.setCapacity(request.capacity());

        Event savedEvent = eventRepository.save(existingEvent);

        return eventMapper.toResponse(savedEvent);
    }

    public EventResponse publishEvent(Long id) {
        Event event = findEventById(id);
        event.publish();

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    public EventResponse cancelEvent(Long id) {
        Event event = findEventById(id);
        event.cancel();

        Event savedEvent = eventRepository.save(event);

        refundService.processAutomaticRefundForCanceledEvent(id);

        return eventMapper.toResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status)
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public void deleteEvent(Long id) {
        Event event = findEventById(id);
        eventRepository.delete(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found by id: " + id
                        )
                );
    }
}
