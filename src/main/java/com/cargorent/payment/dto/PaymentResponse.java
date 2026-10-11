package com.cargorent.payment.dto;

import com.cargorent.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long bookingId, Long userId, BigDecimal amount, String method, String status,
                              String transactionId, String receiptNumber, String gatewayMode, String instrument,
                              String failureReason, String vehicleName, String customerName, LocalDateTime paidAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getBookingId(), p.getUserId(), p.getAmount(), p.getMethod().name(),
                p.getStatus().name(), p.getTransactionId(), p.getReceiptNumber(), p.getGatewayMode(),
                p.getInstrument(), p.getFailureReason(), p.getVehicleName(), p.getCustomerName(), p.getPaidAt());
    }
}
