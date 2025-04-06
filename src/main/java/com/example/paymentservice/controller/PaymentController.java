package com.example.paymentservice.controller;


import com.example.common.ApiResponse;
import com.example.feignapi.dto.RefundDTO;
import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;



    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createPayment(@Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        String alipayPage = paymentService.createPayment(paymentCreateDTO);
        return ResponseEntity.ok(ApiResponse.success("支付账单创建成功", alipayPage));
    }

    @PostMapping("/alipay/notify")
    public ResponseEntity<ApiResponse<String>> alipayNotify(@RequestParam("out_trade_no") String outTradeNo,
                                                            @RequestParam("trade_no") String tradeNo,
                                                            @RequestParam("total_amount") BigDecimal totalAmount,
                                                            @RequestParam("gmt_payment") String timestamp,
                                                            @RequestParam("trade_status") String status) {
        paymentService.alipayNotify(outTradeNo, tradeNo, totalAmount, timestamp, status);
        return ResponseEntity.ok(ApiResponse.success("支付成功"));
    }

    @PostMapping("/refund")
    ResponseEntity<ApiResponse<PaymentVO>> refund(@RequestBody RefundDTO refundDTO){
        PaymentVO paymentVO = paymentService.refund(refundDTO);
        return ResponseEntity.ok(ApiResponse.success("退款成功", paymentVO));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentVO>> getPayment(@PathVariable("bookingId") Long bookingId){
        PaymentVO paymentVO = paymentService.getPayment(bookingId);
        return ResponseEntity.ok(ApiResponse.success("查询成功", paymentVO));
    }

}
