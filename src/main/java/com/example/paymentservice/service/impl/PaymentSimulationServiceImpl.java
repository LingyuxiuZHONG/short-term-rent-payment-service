package com.example.paymentservice.service.impl;


import com.example.common.ApiResponse;
import com.example.common.dto.BookingStatus;
import com.example.common.dto.PaymentStatus;
import com.example.common.exception.BusinessException;
import com.example.feignapi.clients.BookingClient;
import com.example.feignapi.dto.UpdateBookingPaymentDTO;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentSimulationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentSimulationServiceImpl implements PaymentSimulationService {

    private final PaymentMapper paymentMapper;

    private final BookingClient bookingClient;


    @Override
    public void simulatePaymentSuccess(Long paymentId) {
        log.info("开始模拟支付成功，paymentId: {}", paymentId);
        Payment payment = paymentMapper.getPaymentById(paymentId);
        if(payment == null){
            throw new BusinessException("支付订单不存在");
        }

        String transactionId = UUID.randomUUID().toString();
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.update(payment);
        log.info("支付记录更新为成功，paymentId: {}, transactionId: {}", paymentId, transactionId);


        UpdateBookingPaymentDTO updateBookingPaymentDTO = new UpdateBookingPaymentDTO();
        updateBookingPaymentDTO.setBookingId(payment.getBookingId());
        updateBookingPaymentDTO.setPaidAmount(payment.getAmount());
        updateBookingPaymentDTO.setStatus(BookingStatus.CONFIRMED.getCode());
        updateBookingPaymentDTO.setPaymentTransactionId(transactionId);

        ResponseEntity<ApiResponse<String>> response = bookingClient.updateBookingPayment(updateBookingPaymentDTO);
        if(response == null || !(response.getBody().getCode() == 200)){
            log.error("调用booking服务更新支付信息失败，bookingId: {}", payment.getBookingId());
            throw new BusinessException("更新预订订单支付信息失败");
        }

        log.info("支付成功模拟流程结束，paymentId: {}", paymentId);
    }

    @Override
    public void simulatePaymentFailure(Long paymentId) {
        log.info("开始模拟支付失败，paymentId: {}", paymentId);
        Payment payment = paymentMapper.getPaymentById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付订单不存在");
        }

        // 假设支付失败，更新支付状态为失败
        String reason = "支付失败"; // 可以从第三方支付接口获取具体的失败原因
        payment.setStatus(PaymentStatus.FAILED.getCode());
        payment.setReason(reason);
        paymentMapper.update(payment);
        log.info("支付记录更新为失败，paymentId: {}, reason: {}", paymentId, reason);

        log.info("支付失败模拟流程结束，paymentId: {}", paymentId);
    }

    @Override
    public void simulateRefundSuccess(Long paymentId) {
        log.info("开始模拟退款成功，paymentId: {}", paymentId);

        Payment payment = paymentMapper.getPaymentById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付订单不存在");
        }

        String refundTransactionId = UUID.randomUUID().toString();
        payment.setStatus(PaymentStatus.REFUND_SUCCESS.getCode());
        payment.setRefundTransactionId(refundTransactionId);
        payment.setReason("退款成功");
        payment.setRefundedAt(LocalDateTime.now());
        paymentMapper.update(payment); // 更新支付记录
        log.info("支付记录更新为退款成功，paymentId: {}, refundTransactionId: {}", paymentId, refundTransactionId);

        UpdateBookingPaymentDTO updateBookingPaymentDTO = new UpdateBookingPaymentDTO();
        updateBookingPaymentDTO.setBookingId(payment.getBookingId());
        updateBookingPaymentDTO.setPaidAmount(BigDecimal.ZERO);  // 退款后，实际支付金额为零
        updateBookingPaymentDTO.setStatus(BookingStatus.REFUNDED.getCode());  // 订单状态为已退款
        updateBookingPaymentDTO.setRefundTransactionId(refundTransactionId); // 退款流水号

        ResponseEntity<ApiResponse<String>> response = bookingClient.updateBookingPayment(updateBookingPaymentDTO);
        if (response == null || !(response.getBody().getCode() == 200)) {
            log.error("调用booking服务更新支付信息失败，bookingId: {}", payment.getBookingId());
            throw new BusinessException("更新预订订单支付信息失败");
        }

        log.info("退款成功模拟流程结束，paymentId: {}", paymentId);
    }


    @Override
    public void simulateRefundFailure(Long paymentId) {
        log.info("开始模拟退款失败，paymentId: {}", paymentId);

        Payment payment = paymentMapper.getPaymentById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付订单不存在");
        }

        payment.setStatus(PaymentStatus.REFUND_FAILED.getCode());
        payment.setReason("退款失败");
        paymentMapper.update(payment);  // 更新支付记录
        log.info("支付记录更新为退款失败，paymentId: {}", paymentId);

        log.info("退款失败模拟流程结束，paymentId: {}", paymentId);
    }

}
