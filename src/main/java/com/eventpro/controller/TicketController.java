package com.eventpro.controller;

import com.eventpro.dto.TicketResponse;
import com.eventpro.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{code}")
    public TicketResponse getTicketByCode(@PathVariable String code) {
        return ticketService.getTicketByCode(code);
    }

    @GetMapping("/reservation/{reservationId}")
    public List<TicketResponse> getTicketsByReservation(@PathVariable Long reservationId) {
        return ticketService.getTicketsByReservation(reservationId);
    }

    @PatchMapping("/{code}/scan")
    public TicketResponse scanTicket(@PathVariable String code) {
        return ticketService.scanTicket(code);
    }
}

