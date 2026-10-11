package com.cargorent.payment.dto;

import com.cargorent.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Card fields are only used to simulate the charge and are never stored
 * (only the last 4 digits are kept). CVV is never logged or saved.
 */
public record PaymentRequest(
        @NotNull Long bookingId,
        @NotNull PaymentMethod method,
        @Size(max = 23) String cardNumber,
        @Size(max = 100) String cardHolder,
        @Size(max = 5) String expiry,
        @Size(max = 4) String cvv,
        @Size(max = 60) String upiId,
        @Size(max = 60) String bankName) {

    @Override
    public String toString() {
        return "PaymentRequest[bookingId=" + bookingId + ", method=" + method + "]";
    }
}
