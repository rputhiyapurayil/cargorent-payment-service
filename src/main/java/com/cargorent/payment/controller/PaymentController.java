package com.cargorent.payment.controller;

import com.cargorent.payment.common.CurrentUser;
import com.cargorent.payment.dto.PaymentRequest;
import com.cargorent.payment.dto.PaymentResponse;
import com.cargorent.payment.dto.ReceiptResponse;
import com.cargorent.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pay for a booking (test mode). Test card 4111 1111 1111 1111 succeeds, 4000 0000 0000 0002 is declined.")
    public PaymentResponse pay(CurrentUser user, @Valid @RequestBody PaymentRequest req) {
        return paymentService.pay(user, req);
    }

    @GetMapping("/my")
    public List<PaymentResponse> mine(CurrentUser user) {
        return paymentService.mine(user);
    }

    @GetMapping
    @Operation(summary = "Admin: all payment transactions")
    public List<PaymentResponse> all(CurrentUser user) {
        return paymentService.all(user);
    }

    @GetMapping("/booking/{bookingId}")
    public List<PaymentResponse> forBooking(CurrentUser user, @PathVariable Long bookingId) {
        return paymentService.forBooking(user, bookingId);
    }

    @GetMapping("/{id:\\d+}")
    public PaymentResponse get(CurrentUser user, @PathVariable Long id) {
        return paymentService.get(user, id);
    }

    @GetMapping("/{id:\\d+}/receipt")
    public ReceiptResponse receipt(CurrentUser user, @PathVariable Long id) {
        return paymentService.receipt(user, id);
    }
}
