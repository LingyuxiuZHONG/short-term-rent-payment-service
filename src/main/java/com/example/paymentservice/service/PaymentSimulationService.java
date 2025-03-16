package com.example.paymentservice.service;

public interface PaymentSimulationService {
    void simulatePaymentSuccess(Long paymentId);

    void simulatePaymentFailure(Long paymentId);

    void simulateRefundSuccess(Long paymentId);

    void simulateRefundFailure(Long paymentId);
}
