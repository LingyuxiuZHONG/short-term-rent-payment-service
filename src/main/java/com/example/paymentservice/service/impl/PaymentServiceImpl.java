package com.example.paymentservice.service.impl;

import com.example.common.dto.BookingStatus;
import com.example.common.dto.PaymentStatus;
import com.example.common.exception.BusinessException;
import com.example.feignapi.clients.BookingClient;
import com.example.feignapi.vo.BookingVO;
import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final BookingClient bookingClient;

    private final PaymentMapper paymentMapper;


    private final static List<String> ALLOWED_PAYMENT_METHOD = Arrays.asList("wechat", "alipay", "simulation");



    @Override
    public PaymentVO createPayment(PaymentCreateDTO paymentCreateDTO) {
        log.info("创建支付记录开始，订单ID：{}", paymentCreateDTO.getBookingId());

        // 1. 检查预订订单是否存在
        BookingVO booking = bookingClient.checkIfBookingExists(paymentCreateDTO.getBookingId()).getBody().getData();
        if (booking == null) {
            log.warn("预订订单不存在，订单ID：{}", paymentCreateDTO.getBookingId());
            throw new BusinessException("预订订单不存在");
        }
        log.info("预订订单存在，订单ID：{}", paymentCreateDTO.getBookingId());

        // 2. 检查订单状态
        if (!booking.getStatus().equals(BookingStatus.PENDING_PAYMENT.getCode())) {
            log.warn("订单状态不符合支付条件，订单ID：{}，状态：{}", paymentCreateDTO.getBookingId(), booking.getStatus());
            throw new BusinessException("当前订单状态无法创建支付记录");
        }
        log.info("订单状态符合支付条件，订单ID：{}", paymentCreateDTO.getBookingId());

        // 3. 检查支付金额
        BigDecimal amountNeedToPay = booking.getTotalAmount().subtract(booking.getDiscountAmount());
        if (paymentCreateDTO.getAmount().compareTo(amountNeedToPay) != 0) {
            log.warn("支付金额与订单金额不匹配，订单ID：{}，支付金额：{}，应付金额：{}",
                    paymentCreateDTO.getBookingId(), paymentCreateDTO.getAmount(), amountNeedToPay);
            throw new BusinessException("支付金额与订单金额不匹配");
        }
        log.info("支付金额匹配，订单ID：{}", paymentCreateDTO.getBookingId());

        // 4. 检查支付方式
        if (!ALLOWED_PAYMENT_METHOD.contains(paymentCreateDTO.getPaymentMethod())) {
            log.warn("不支持此支付方式，订单ID：{}，支付方式：{}", paymentCreateDTO.getBookingId(), paymentCreateDTO.getPaymentMethod());
            throw new BusinessException("不支持此支付方式");
        }
        log.info("支付方式合法，订单ID：{}", paymentCreateDTO.getBookingId());

        // 5. 检查是否已存在支付成功/退款成功的支付记录
        Payment paymentExist = paymentMapper.getPaymentByBookingId(paymentCreateDTO.getBookingId());
        if (paymentExist != null && paymentExist.getStatus().equals(PaymentStatus.SUCCESS)) {
            log.warn("已存在有效的支付记录，订单ID：{}", paymentCreateDTO.getBookingId());
            throw new BusinessException("当前订单已经存在有效的支付记录");
        }
        log.info("没有有效的支付记录，订单ID：{}", paymentCreateDTO.getBookingId());

        // 创建支付记录
        Payment payment = new Payment();
        BeanUtils.copyProperties(paymentCreateDTO, payment);
        payment.setStatus(PaymentStatus.PROCESSING.getCode());
        paymentMapper.insert(payment);

        log.info("支付记录创建成功，订单ID：{}", paymentCreateDTO.getBookingId());

        return convertToPaymentVO(payment);
    }



    private PaymentVO convertToPaymentVO(Payment payment) {
        PaymentVO paymentVO = new PaymentVO();
        BeanUtils.copyProperties(payment,paymentVO);
        paymentVO.setPayUrl(generateFakePayUrl(payment.getId()));
        return paymentVO;
    }

    private String generateFakePayUrl(Long paymentId) {
        // 假设生成一个假的支付链接（模拟支付平台的行为）
        String url1 = "http://localhost:8086/api/payments/" + paymentId + "/simulateSuccess";
        String url2 = "http://localhost:8086/api/payments/" + paymentId + "/simulateFailure";
        boolean b = new Random().nextBoolean();
        return b ? url1 : url2;
    }

}
