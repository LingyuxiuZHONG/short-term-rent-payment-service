package com.example.paymentservice.service;

import com.example.feignapi.dto.RefundDTO;
import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;

import java.math.BigDecimal;

public interface PaymentService {
    String createPayment(PaymentCreateDTO paymentCreateDTO);

    PaymentVO getPayment(Long bookingId);

    void alipayNotify(String outTradeNo, String tradeNo, BigDecimal totalAmount, String timestamp, String status);

    PaymentVO refund(RefundDTO refundDTO);
}
