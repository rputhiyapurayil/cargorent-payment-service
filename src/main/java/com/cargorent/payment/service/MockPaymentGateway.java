package com.cargorent.payment.service;

import com.cargorent.payment.common.ApiException;
import com.cargorent.payment.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Test-mode payment gateway (no real money moves). Behaves like Razorpay/Stripe test mode:
 * <ul>
 *   <li>Any valid test card (passes the Luhn check, future expiry) succeeds, e.g. 4111 1111 1111 1111.</li>
 *   <li>Card 4000 0000 0000 0002 is always declined.</li>
 *   <li>UPI id "failure@upi" is declined; any other valid UPI id succeeds.</li>
 * </ul>
 * Invalid input (bad card number, expired card) is a 400 error, not a failed payment.
 */
@Component
public class MockPaymentGateway {

    public static final String DECLINED_CARD = "4000000000000002";
    public static final String DECLINED_UPI = "failure@upi";

    public record Result(boolean success, String transactionId, String instrument, String failureReason) { }

    public Result charge(PaymentRequest req) {
        return switch (req.method()) {
            case CARD -> card(req);
            case UPI -> upi(req);
            case NET_BANKING -> netBanking(req);
        };
    }

    private Result card(PaymentRequest req) {
        String number = req.cardNumber() == null ? "" : req.cardNumber().replaceAll("[\\s-]", "");
        if (!number.matches("\\d{13,19}") || !luhnValid(number)) throw ApiException.badRequest("Invalid card number");
        if (req.cardHolder() == null || req.cardHolder().isBlank()) throw ApiException.badRequest("Card holder name is required");
        if (req.cvv() == null || !req.cvv().matches("\\d{3,4}")) throw ApiException.badRequest("Invalid CVV");
        YearMonth expiry = parseExpiry(req.expiry());
        if (expiry.isBefore(YearMonth.now())) throw ApiException.badRequest("Card has expired");

        String instrument = network(number) + " •••• " + number.substring(number.length() - 4);
        if (DECLINED_CARD.equals(number)) {
            return new Result(false, txnId(), instrument, "Card declined by issuing bank (test card)");
        }
        return new Result(true, txnId(), instrument, null);
    }

    private Result upi(PaymentRequest req) {
        String upi = req.upiId() == null ? "" : req.upiId().trim().toLowerCase();
        if (!upi.matches("[a-z0-9.\\-_]{2,256}@[a-z]{2,64}")) throw ApiException.badRequest("Invalid UPI ID");
        if (DECLINED_UPI.equals(upi)) return new Result(false, txnId(), upi, "UPI payment declined (test UPI ID)");
        return new Result(true, txnId(), upi, null);
    }

    private Result netBanking(PaymentRequest req) {
        if (req.bankName() == null || req.bankName().isBlank()) throw ApiException.badRequest("Select a bank");
        return new Result(true, txnId(), "Net banking - " + req.bankName().trim(), null);
    }

    static boolean luhnValid(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }

    private static YearMonth parseExpiry(String expiry) {
        try {
            return YearMonth.parse(expiry == null ? "" : expiry.trim(), DateTimeFormatter.ofPattern("MM/yy"));
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("Expiry must be in MM/YY format");
        }
    }

    private static String network(String number) {
        if (number.startsWith("4")) return "VISA";
        if (number.matches("^5[1-5].*") || number.matches("^2[2-7].*")) return "MASTERCARD";
        if (number.matches("^(60|65|81|82|508).*")) return "RUPAY";
        if (number.matches("^3[47].*")) return "AMEX";
        return "CARD";
    }

    private static String txnId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
