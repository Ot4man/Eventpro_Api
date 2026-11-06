package com.eventpro.controller;

import com.eventpro.dto.ReservationCreateRequest;
import com.eventpro.dto.ReservationResponse;
import com.eventpro.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id) {
        return reservationService.getReservation(id);
    }

    @GetMapping("/client/{clientId}")
    public List<ReservationResponse> getReservationsByClient(@PathVariable Long clientId) {
        return reservationService.getReservationsByClient(clientId);
    }

    @PatchMapping("/{id}/confirm")
    public ReservationResponse confirmReservation(@PathVariable Long id) {
        return reservationService.confirmReservation(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }
}
