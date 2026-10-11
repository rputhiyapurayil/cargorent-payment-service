package com.cargorent.payment.client;

import com.cargorent.payment.common.ApiException;
import com.cargorent.payment.dto.BookingInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class BookingClient {

    private final RestClient rest;

    public BookingClient(RestClient.Builder builder, @Value("${services.booking.url}") String baseUrl) {
        this.rest = builder.baseUrl(baseUrl).build();
    }

    public BookingInfo get(Long bookingId) {
        try {
            return rest.get().uri("/bookings/{id}", bookingId).retrieve().body(BookingInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.notFound("Booking not found");
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Booking service is unavailable. Please try again.");
        }
    }

    /** Marks the booking CONFIRMED. Throws if booking-service refuses (e.g. booking expired). */
    public void confirm(Long bookingId, Long paymentId) {
        rest.patch().uri("/bookings/{id}/status", bookingId)
                .body(Map.of("status", "CONFIRMED", "paymentId", paymentId))
                .retrieve().toBodilessEntity();
    }
}
