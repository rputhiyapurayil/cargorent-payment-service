package com.cargorent.payment.service;

import com.cargorent.payment.client.BookingClient;
import com.cargorent.payment.common.ApiException;
import com.cargorent.payment.common.CurrentUser;
import com.cargorent.payment.dto.*;
import com.cargorent.payment.entity.Payment;
import com.cargorent.payment.entity.PaymentStatus;
import com.cargorent.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final DateTimeFormatter RECEIPT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository payments;
    private final BookingClient bookingClient;
    private final MockPaymentGateway gateway;

    @Value("${cargorent.company.name}")
    private String companyName;
    @Value("${cargorent.company.address}")
    private String companyAddress;
    @Value("${cargorent.company.email}")
    private String companyEmail;

    /**
     * Pays for a booking. Not one DB transaction on purpose: the payment row must be kept
     * even if confirming the booking afterwards fails, so the money can be traced and refunded.
     */
    public PaymentResponse pay(CurrentUser user, PaymentRequest req) {
        Long userId = user.requireId();
        BookingInfo booking = bookingClient.get(req.bookingId());
        if (!booking.userId().equals(userId)) throw ApiException.notFound("Booking not found");
        if (!"PENDING_PAYMENT".equals(booking.status())) {
            throw ApiException.conflict("This booking is " + booking.status().toLowerCase().replace('_', ' ')
                    + " and cannot be paid");
        }
        if (payments.existsByBookingIdAndStatus(booking.id(), PaymentStatus.SUCCESS)) {
            throw ApiException.conflict("This booking has already been paid");
        }

        MockPaymentGateway.Result result = gateway.charge(req);

        Payment p = new Payment();
        p.setBookingId(booking.id());
        p.setUserId(userId);
        p.setAmount(booking.totalAmount());
        p.setMethod(req.method());
        p.setGatewayMode("TEST");
        p.setTransactionId(result.transactionId());
        p.setInstrument(result.instrument());
        p.setStatus(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        p.setFailureReason(result.failureReason());
        p.setCustomerName(booking.customerName());
        p.setCustomerEmail(booking.customerEmail());
        p.setVehicleName(booking.vehicleName());
        p.setRegistrationNumber(booking.registrationNumber());
        p.setStartDate(booking.startDate());
        p.setEndDate(booking.endDate());
        p.setDays(booking.days());
        p.setPricePerDay(booking.pricePerDay());
        p.setSubtotal(booking.subtotal());
        p.setDiscount(booking.discount());
        payments.save(p);

        if (!result.success()) {
            log.info("Payment {} for booking {} declined", p.getId(), booking.id());
            return PaymentResponse.from(p);
        }

        p.setReceiptNumber("CGR-" + p.getPaidAt().format(RECEIPT_DATE) + "-" + String.format("%05d", p.getId()));
        payments.save(p);

        try {
            bookingClient.confirm(booking.id(), p.getId());
        } catch (Exception e) {
            log.warn("Payment {} succeeded but booking {} could not be confirmed: {}", p.getId(), booking.id(), e.getMessage());
            p.setStatus(PaymentStatus.REFUNDED);
            p.setFailureReason("Booking could not be confirmed (it may have expired). Amount refunded.");
            payments.save(p);
            throw ApiException.conflict(p.getFailureReason());
        }
        log.info("Payment {} for booking {} succeeded", p.getId(), booking.id());
        return PaymentResponse.from(p);
    }

    public List<PaymentResponse> mine(CurrentUser user) {
        return payments.findByUserIdOrderByPaidAtDesc(user.requireId()).stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> all(CurrentUser user) {
        user.requireAdmin();
        return payments.findAllByOrderByPaidAtDesc().stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> forBooking(CurrentUser user, Long bookingId) {
        List<Payment> list = payments.findByBookingIdOrderByPaidAtDesc(bookingId);
        if (!user.isAdmin()) {
            Long userId = user.requireId();
            list = list.stream().filter(p -> p.getUserId().equals(userId)).toList();
        }
        return list.stream().map(PaymentResponse::from).toList();
    }

    public PaymentResponse get(CurrentUser user, Long id) {
        return PaymentResponse.from(findVisible(user, id));
    }

    public ReceiptResponse receipt(CurrentUser user, Long id) {
        Payment p = findVisible(user, id);
        if (p.getStatus() == PaymentStatus.FAILED) throw ApiException.badRequest("No receipt for a failed payment");
        return new ReceiptResponse(p.getReceiptNumber(), p.getTransactionId(), p.getPaidAt(), p.getStatus().name(),
                p.getGatewayMode(), p.getMethod().name(), p.getInstrument(),
                companyName, companyAddress, companyEmail,
                p.getBookingId(), p.getCustomerName(), p.getCustomerEmail(),
                p.getVehicleName(), p.getRegistrationNumber(), p.getStartDate(), p.getEndDate(),
                p.getDays(), p.getPricePerDay(), p.getSubtotal(), p.getDiscount(), p.getAmount());
    }

    private Payment findVisible(CurrentUser user, Long id) {
        Payment p = payments.findById(id).orElseThrow(() -> ApiException.notFound("Payment not found"));
        if (!user.isAdmin() && !p.getUserId().equals(user.requireId())) throw ApiException.notFound("Payment not found");
        return p;
    }
}
