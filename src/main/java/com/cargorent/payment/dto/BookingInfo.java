package com.cargorent.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The subset of booking-service's booking JSON that this service reads. */
public record BookingInfo(Long id, Long userId, String customerName, String customerEmail, Long vehicleId,
                          String vehicleName, String registrationNumber, LocalDate startDate, LocalDate endDate,
                          int days, BigDecimal pricePerDay, BigDecimal subtotal, BigDecimal discount,
                          BigDecimal totalAmount, String status) {
}
