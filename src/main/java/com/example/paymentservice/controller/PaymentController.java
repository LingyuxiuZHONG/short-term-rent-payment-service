package com.example.paymentservice.controller;


import com.example.common.ApiResponse;
import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;
import com.example.paymentservice.service.PaymentService;
import com.example.paymentservice.service.PaymentSimulationService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private final PaymentSimulationService paymentSimulationService;


    @PostMapping("")
    public ResponseEntity<ApiResponse<PaymentVO>> createPayment(@Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        PaymentVO paymentVO = paymentService.createPayment(paymentCreateDTO);
        return ResponseEntity.ok(ApiResponse.success("支付账单创建成功", paymentVO));
    }

    // 模拟支付成功
    @PostMapping("/{paymentId}/simulateSuccess")
    public ResponseEntity<ApiResponse<String>> simulatePaymentSuccess(@PathVariable Long paymentId) {
        paymentSimulationService.simulatePaymentSuccess(paymentId);
        return ResponseEntity.ok(ApiResponse.success("支付成功模拟完成"));
    }

    // 模拟支付失败
    @PostMapping("/{paymentId}/simulateFailure")
    public ResponseEntity<ApiResponse<String>> simulatePaymentFailure(@PathVariable Long paymentId) {
        paymentSimulationService.simulatePaymentFailure(paymentId);
        return ResponseEntity.ok(ApiResponse.success("支付失败模拟完成"));
    }

    // 模拟退款成功
    @PostMapping("/{paymentId}/simulateRefundSuccess")
    public ResponseEntity<ApiResponse<String>> simulateRefundSuccess(@PathVariable Long paymentId) {
        paymentSimulationService.simulateRefundSuccess(paymentId);
        return ResponseEntity.ok(ApiResponse.success("退款成功模拟完成"));
    }

    // 模拟退款失败
    @PostMapping("/{paymentId}/simulateRefundFailure")
    public ResponseEntity<ApiResponse<String>> simulateRefundFailure(@PathVariable Long paymentId) {
        paymentSimulationService.simulateRefundFailure(paymentId);
        return ResponseEntity.ok(ApiResponse.success("退款失败模拟完成"));
    }

}
