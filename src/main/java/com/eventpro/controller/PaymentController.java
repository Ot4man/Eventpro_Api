package com.eventpro.controller;

import com.eventpro.dto.PaymentRequest;
import com.eventpro.dto.PaymentResponse;
import com.eventpro.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/reservation/{reservationId}")
    public List<PaymentResponse> getPaymentsByReservation(@PathVariable Long reservationId) {
        return paymentService.getPaymentsByReservation(reservationId);
    }
}

