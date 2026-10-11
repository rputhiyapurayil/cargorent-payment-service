package com.cargorent.payment.entity;

/** REFUNDED: money was taken but the booking could not be confirmed (e.g. it expired meanwhile). */
public enum PaymentStatus { SUCCESS, FAILED, REFUNDED }
