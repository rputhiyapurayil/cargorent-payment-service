package com.cargorent.payment.service;

import com.cargorent.payment.common.ApiException;
import com.cargorent.payment.dto.PaymentRequest;
import com.cargorent.payment.entity.PaymentMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockPaymentGatewayTest {

    private final MockPaymentGateway gateway = new MockPaymentGateway();

    private static PaymentRequest card(String number, String expiry) {
        return new PaymentRequest(1L, PaymentMethod.CARD, number, "Test User", expiry, "123", null, null);
    }

    @Test
    void validTestCardSucceedsAndIsMasked() {
        var r = gateway.charge(card("4111 1111 1111 1111", "12/30"));
        assertThat(r.success()).isTrue();
        assertThat(r.instrument()).isEqualTo("VISA •••• 1111");
        assertThat(r.transactionId()).startsWith("TXN");
    }

    @Test
    void declineTestCardFails() {
        var r = gateway.charge(card("4000000000000002", "12/30"));
        assertThat(r.success()).isFalse();
        assertThat(r.failureReason()).contains("declined");
    }

    @Test
    void invalidCardNumberIsRejected() {
        assertThatThrownBy(() -> gateway.charge(card("4111 1111 1111 1112", "12/30")))
                .isInstanceOf(ApiException.class).hasMessageContaining("Invalid card number");
    }

    @Test
    void expiredCardIsRejected() {
        assertThatThrownBy(() -> gateway.charge(card("4111111111111111", "01/20")))
                .isInstanceOf(ApiException.class).hasMessageContaining("expired");
    }

    @Test
    void upiFailureIdIsDeclined() {
        var ok = gateway.charge(new PaymentRequest(1L, PaymentMethod.UPI, null, null, null, null, "asha@okaxis", null));
        var bad = gateway.charge(new PaymentRequest(1L, PaymentMethod.UPI, null, null, null, null, "failure@upi", null));
        assertThat(ok.success()).isTrue();
        assertThat(bad.success()).isFalse();
    }

    @Test
    void luhnCheck() {
        assertThat(MockPaymentGateway.luhnValid("4111111111111111")).isTrue();
        assertThat(MockPaymentGateway.luhnValid("4111111111111112")).isFalse();
    }
}
