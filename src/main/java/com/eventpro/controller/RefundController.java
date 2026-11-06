package com.eventpro.controller;

import com.eventpro.dto.RefundRequest;
import com.eventpro.dto.RefundResponse;
import com.eventpro.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse requestRefund(@Valid @RequestBody RefundRequest request) {
        return refundService.requestRefund(request);
    }

    @GetMapping("/{id}")
    public RefundResponse getRefundById(@PathVariable Long id) {
        return refundService.getRefundById(id);
    }

    @GetMapping("/payment/{paymentId}")
    public List<RefundResponse> getRefundsByPayment(@PathVariable Long paymentId) {
        return refundService.getRefundsByPayment(paymentId);
    }
}

