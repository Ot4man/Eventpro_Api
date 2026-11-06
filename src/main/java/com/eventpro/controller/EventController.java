package com.eventpro.controller;

import com.eventpro.dto.EventCreateRequest;
import com.eventpro.dto.EventResponse;
import com.eventpro.dto.EventUpdateRequest;
import com.eventpro.entity.enums.EventStatus;
import com.eventpro.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @Valid @RequestBody EventCreateRequest request
    ) {
        return eventService.createEvent(request);
    }
    @GetMapping
    public List<EventResponse> getAllEvents(){
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id){
        return eventService.getEventById(id);
    }
    @GetMapping("/status/{status}")
    public List<EventResponse> getEventStatus(@PathVariable EventStatus status){
        return eventService.getEventsByStatus(status);
    }
    @PatchMapping("/{id}/publish")
    public EventResponse publishEvent(@PathVariable Long id) {
        return eventService.publishEvent(id);
    }

    @PatchMapping("/{id}/cancel")
    public EventResponse cancelEvent(@PathVariable Long id) {
        return eventService.cancelEvent(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        return eventService.updateEvent(id, request);
    }

}
