package com.cargorent.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptResponse(String receiptNumber, String transactionId, LocalDateTime paidAt, String status,
                              String gatewayMode, String method, String instrument,
                              String companyName, String companyAddress, String companyEmail,
                              Long bookingId, String customerName, String customerEmail,
                              String vehicleName, String registrationNumber, LocalDate startDate, LocalDate endDate,
                              Integer days, BigDecimal pricePerDay, BigDecimal subtotal, BigDecimal discount,
                              BigDecimal amountPaid) {
}
