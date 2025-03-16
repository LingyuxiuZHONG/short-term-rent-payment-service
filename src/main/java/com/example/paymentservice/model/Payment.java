package com.example.paymentservice.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private Long bookingId;
    private String paymentMethod;  // 支付方式：wechat, alipay
    private BigDecimal amount;     // 支付金额
    private Integer status;        // 支付状态：0支付中，1支付成功，2支付失败，3退款成功，4退款失败
    private String transactionId;  // 第三方支付平台交易号
    private String reason;         // 支付/退款失败原因
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private String refundTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}
