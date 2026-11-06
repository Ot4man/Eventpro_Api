package com.eventpro.controller;

import com.eventpro.dto.EventStatsResponse;
import com.eventpro.dto.OrganizerStatsResponse;
import com.eventpro.service.OrganizerStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class OrganizerStatsController {

    private final OrganizerStatsService statsService;

    public OrganizerStatsController(OrganizerStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/events/{eventId}")
    public EventStatsResponse getEventStats(@PathVariable Long eventId) {
        return statsService.getEventStats(eventId);
    }

    @GetMapping("/organizers/{organizerId}")
    public OrganizerStatsResponse getOrganizerStats(@PathVariable Long organizerId) {
        return statsService.getOrganizerStats(organizerId);
    }
}

