package com.cargorent.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 40, unique = true)
    private String transactionId;

    @Column(name = "receipt_number", length = 30, unique = true)
    private String receiptNumber;

    /** "TEST" for the built-in mock gateway. */
    @Column(name = "gateway_mode", nullable = false, length = 10)
    private String gatewayMode;

    /** Masked instrument, e.g. "VISA •••• 1111" or "rahul@okaxis". Never the full card number. */
    @Column(name = "instrument", length = 60)
    private String instrument;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    // Booking snapshot for receipts
    @Column(name = "customer_name", length = 100)
    private String customerName;
    @Column(name = "customer_email", length = 150)
    private String customerEmail;
    @Column(name = "vehicle_name", length = 120)
    private String vehicleName;
    @Column(name = "registration_number", length = 20)
    private String registrationNumber;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    private Integer days;
    @Column(name = "price_per_day", precision = 10, scale = 2)
    private BigDecimal pricePerDay;
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;
    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @PrePersist
    void onCreate() {
        if (paidAt == null) paidAt = LocalDateTime.now();
    }
}
