package com.example.paymentservice.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.example.common.dto.BookingStatus;
import com.example.common.dto.PaymentStatus;
import com.example.common.exception.BusinessException;
import com.example.feignapi.clients.BookingClient;
import com.example.feignapi.dto.RefundDTO;
import com.example.feignapi.vo.BookingUpdateAfterPayDTO;
import com.example.feignapi.vo.BookingVO;
import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentService;
import com.example.paymentservice.util.PayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final BookingClient bookingClient;

    private final PaymentMapper paymentMapper;

    private final PayUtil payUtil;


    private final static List<String> ALLOWED_PAYMENT_METHOD = Arrays.asList("wechat", "alipay", "simulation");



    @Override
    public String createPayment(PaymentCreateDTO paymentCreateDTO) {

        try {
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

            // 6. **更新订单状态为 "支付中（PROCESSING_PAYMENT）"**
            bookingClient.updateBookingStatus(paymentCreateDTO.getBookingId(), BookingStatus.PROCESSING_PAYMENT.getCode());
            log.info("订单状态更新为 '支付中'，订单ID：{}", paymentCreateDTO.getBookingId());


            // 7. **生成支付订单号**
            String outTradeNo = generateOutTradeNo(paymentCreateDTO.getBookingId());


            // 8. **创建支付记录**
            Payment payment = new Payment();
            BeanUtils.copyProperties(paymentCreateDTO, payment);
            payment.setStatus(PaymentStatus.PROCESSING.getCode());
            payment.setOutTradeNo(outTradeNo);
            paymentMapper.insert(payment);
            log.info("支付记录创建成功，订单ID：{}", paymentCreateDTO.getBookingId());

            // 9. **请求支付宝Api**
            BigDecimal amount = paymentCreateDTO.getAmount();
            String subject = "订单支付";
            String paymentPageHtml;
            paymentPageHtml = payUtil.sendRequestToAlipay(outTradeNo, amount, subject);
            log.info("支付宝支付请求成功，订单ID：{}", paymentCreateDTO.getBookingId());

            // 10. **返回支付页面 HTML**
            return paymentPageHtml;
        } catch (Exception e) {
            log.error("创建支付订单失败，订单ID：{}", paymentCreateDTO.getBookingId(), e);
            throw new BusinessException("创建支付订单失败");
        }

    }

    @Override
    public void alipayNotify(String outTradeNo, String tradeNo, BigDecimal totalAmount, String timestamp, String status) {
        try {
            log.info("【支付宝回调】接收到支付结果: outTradeNo={}, tradeNo={}, totalAmount={}, timestamp={}, status={}",
                    outTradeNo, tradeNo, totalAmount, timestamp, status);

            // 查询支付记录
            Payment payment = paymentMapper.getByOutTradeNo(outTradeNo);
            if (payment == null) {
                log.warn("【支付宝回调】支付记录不存在，outTradeNo={}", outTradeNo);
                throw new BusinessException("支付记录不存在");
            }

            // 判断支付状态
            if ("TRADE_SUCCESS".equals(status)) {

                log.info("【支付宝回调】更新支付记录: outTradeNo={}, tradeNo={}, amount={}", outTradeNo, tradeNo, totalAmount);
                payment.setStatus(PaymentStatus.SUCCESS.getCode());
                payment.setTransactionId(tradeNo);
                payment.setPaidAt(decodeTimestamp(timestamp));
                payment.setAmount(totalAmount);
                paymentMapper.update(payment);

                log.info("【支付宝回调】更新订单状态: bookingId={}, status={}", payment.getBookingId(), BookingStatus.CONFIRMED.getCode());
                bookingClient.updateBookingAfterPay(payment.getBookingId(), new BookingUpdateAfterPayDTO(BookingStatus.CONFIRMED.getCode(), tradeNo, totalAmount));

            } else if ("TRADE_CLOSED".equals(status)) {
                log.warn("【支付宝回调】交易已关闭（超时未支付或已退款），outTradeNo={}", outTradeNo);
                payment.setStatus(PaymentStatus.FAILED.getCode());
                if (tradeNo != null) {
                    payment.setTransactionId(tradeNo);
                }
                paymentMapper.update(payment);

            }

            log.info("【支付宝回调】处理完成: outTradeNo={}", outTradeNo);

        } catch (Exception e) {
            log.error("【支付宝回调】处理支付宝返回结果失败, outTradeNo={}, 错误: {}", outTradeNo, e.getMessage(), e);
            throw new BusinessException("处理支付宝返回结果失败");
        }
    }

    @Override
    public PaymentVO refund(RefundDTO refundDTO) {
        try{
            Long bookingId = refundDTO.getBookingId();
            BigDecimal refundAmount = refundDTO.getRefundAmount();
            log.info("支付服务：开始处理退款，订单ID：{}", bookingId);

            // 1. 查询支付记录
            Payment payment = paymentMapper.getPaymentByBookingId(bookingId);
            if (payment == null || !payment.getStatus().equals(PaymentStatus.SUCCESS.getCode())) {
                log.warn("未找到成功的支付记录，订单ID：{}", bookingId);
                throw new BusinessException("未找到成功的支付记录");
            }

            // 2. 校验退款金额
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                log.warn("退款金额超出支付金额，订单ID：{}，退款金额：{}，支付金额：{}", bookingId, refundAmount, payment.getAmount());
                throw new BusinessException("退款金额不能超过支付金额");
            }

            // 3. 判断是否要调用支付宝退款 API
            if (refundDTO.getRefundAmount() != null && refundDTO.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                log.info("调用支付宝退款 API，订单ID：{}，退款金额：{}", bookingId, refundAmount);
                AlipayTradeRefundResponse refundResponse = payUtil.sendRefundRequestToAlipay(payment.getOutTradeNo(), refundAmount, refundDTO.getReason());
                if (!refundResponse.isSuccess()) {
                    log.error("支付宝退款失败，订单ID：{}，错误信息：{}", bookingId, refundResponse.getMsg());
                    throw new BusinessException("支付宝退款失败：" + refundResponse.getMsg());
                }
                String refundTransactionId = refundResponse.getTradeNo();
                log.info("支付宝退款成功，订单ID：{}，交易ID：{}", bookingId,refundTransactionId);
                // 4. 更新支付记录状态
                payment.setRefundTransactionId(refundResponse.getTradeNo());
                payment.setRefundedAt(refundResponse.getGmtRefundPay().toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime());

            }else{
                payment.setRefundedAt(LocalDateTime.now());
            }
            payment.setStatus(PaymentStatus.REFUND_SUCCESS.getCode());
            payment.setRefundAmount(refundAmount);
            payment.setReason(refundDTO.getReason());

            paymentMapper.update(payment);
            log.info("支付记录更新成功，订单ID：{}", bookingId);
            return convertToPaymentVO(payment);
        } catch (AlipayApiException e){
            throw new BusinessException("支付宝退款失败");
        }

    }



    @Override
    public PaymentVO getPayment(Long bookingId) {

        Payment payment = paymentMapper.getPaymentByBookingId(bookingId);
        return convertToPaymentVO(payment);
    }


    private LocalDateTime decodeTimestamp(String timestamp) throws UnsupportedEncodingException {
        // 1. 解码 URL 编码的 timestamp
        String decodedTimestamp = URLDecoder.decode(timestamp, "UTF-8");

        // 2. 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 3. 将解码后的 timestamp 转换为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(decodedTimestamp, formatter);

        return dateTime;
    }

    // 生成唯一的支付订单号
    private String generateOutTradeNo(Long bookingId) {
        // 例如：使用预订订单ID + 当前时间戳来生成唯一的支付订单号
        return bookingId + "_" + System.currentTimeMillis();
    }


    private PaymentVO convertToPaymentVO(Payment payment) {
        PaymentVO paymentVO = new PaymentVO();
        BeanUtils.copyProperties(payment,paymentVO);
        return paymentVO;
    }


}
