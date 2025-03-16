package com.example.paymentservice.service;

import com.example.feignapi.vo.PaymentVO;
import com.example.paymentservice.dto.PaymentCreateDTO;

public interface PaymentService {
    PaymentVO createPayment(PaymentCreateDTO paymentCreateDTO);

}
