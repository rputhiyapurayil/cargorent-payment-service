package com.cargorent.payment.repository;

import com.cargorent.payment.entity.Payment;
import com.cargorent.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByPaidAtDesc(Long userId);
    List<Payment> findByBookingIdOrderByPaidAtDesc(Long bookingId);
    List<Payment> findAllByOrderByPaidAtDesc();
    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
