package com.example.paymentservice.mapper;


import com.example.paymentservice.model.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {
    // 插入支付记录
    void insert(Payment payment);

    // 更新支付状态
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("transactionId") String transactionId, @Param("reason") String reason);

    // 根据booking_id查询支付记录
    Payment getPaymentByBookingId(Long bookingId);

    Payment getPaymentById(Long paymentId);

    void update(Payment payment);
}
