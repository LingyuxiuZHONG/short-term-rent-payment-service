package com.example.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateDTO {
    @NotNull
    private Long bookingId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String paymentMethod;
}

