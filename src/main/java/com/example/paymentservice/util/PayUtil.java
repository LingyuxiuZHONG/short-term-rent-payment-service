package com.example.paymentservice.util;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayUtil {
    //appid
    private final String APP_ID = "9021000140655265";
    //应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDSvzFhqSdrjfoHvSQJjD17dAREn2YI+Viw2b+0NfW/fSCDyz1ZB56Qr/IfknFE7ol8j6CTOa8zIG8pVSBGr3eUJrk67YtnTUAFEqe7HCmj85f+JLXTZNOOGLZolEnO8GjOUVgh5gh2BWEzz46oPZAIiB61El389jnPg9c0FUxmfuOZ5DO3Fh7GSUhXI455LVlih087H1zYzvpDrRqEjo+hj6tNklCRYOFeLCfrU3R0M5lBmg2js9uEOSHjGGgtBaim6eeT2GrTJiVwCCFY6XfYEJ0TQdDExuE26qMrbnvqQ2TMlin5FNe1lxhexDJIPQ+8TQ+B7CmmTdLjwK8yVJvBAgMBAAECggEADk5+arPSAkIsJBRZ+u/zSIsysvnjMl7C/5Z4piI3oZ079NZ6Pq3+vLHFDRGi8NwEp1kUvIng8+aSxUqLG2FUC9GpJ2/y07txyiZfDpu10/R4b/9L/KLOncndsDHI5Ox7IO5yvjJghqqM07iWZaNwQLQE6aPqj36jpAu+M0Hy4s5AzC4Icu1kMrGiNpZtMpd4xYYeIjV8/qDyJJp6mnWbIww0yDUDuPA+KrOM2x79n1qfG3nI/fxznU0wFQSKqPDmYPSdvgbm8HUMMquHTfaH33Q8Gv6ulI7AgiKnWUnAf9UDTzsmcdJAEb2tev8RM3OfZrN3rWlvuji6krDmU6kCBQKBgQD2M2kHpuVmMg1ol9cZc55Hr8o/J0MpXAMO170IEs0vGgVOkpgJtY84EHaCE1TexA+04Tgijimar9nYdOSRx8UbDgp7NHifb7Bz86MANpEJtjV784p9b2T8fX6ExckzG5/P/wAhNpt+cQJa4jtbPS2wIWwzHN1V+hkJP/VcIHgO7wKBgQDbIojyHykA2Hy6Y/G6L1pIej1XaN64oXQgCkFAIq6Vmin9phoZ3UJviRuxiyDk2Dt1Fikn6wybTFtLYALyN7R62NvQzVsFQQ0LyLXNq+DBG3Z1PFOELAx/sFJQADHtSIn3pP5CeZZMUlkOshLem9fpkdobWgDk1u7UYDbApzgATwKBgQCnaYX94gsTVu2vNbDaabgzXuHT18rfkOWzbhfoKYDEipkZOK2RzJe6s9ch1Ctd30we3xbgyHKZ8QHdIn9acdEh+IZACQoMwFHoRr+MIY62X+Q2iQCfEuREnMEvX57U1e/x66AW9Z7+d7H2QufBvvQWVGPSzcnj8NCdbZRWNO3umQKBgG8Om5UJ0cAJ93yHNnUDlp/ww7HPBkFQIggy6krUyOIs1WcljUjaZ9cbB5v9RNh19fwrFQSUDTmPgx06NYQIU0GHMjAqQxzwkOoN+IWZvDhh8LENt83efR8hfzXoQ2VcQ//r8KhD8rYPbe8StJl5Jf3L21vNS3Kusy+S62zL0oflAoGBAJ+iX2jJcW0ndbzYefp4whyDVs6fTKuUHlIxH7e2zywwM1Czul98IYyChCUQLqVCJm8LNLZgYejhxODs1thcLHtaf1NSm7bzxqrUzz6z92MEuo7ijmBJDopVfdp8p//HHcSwf+6rM93tJa63LL4+BPZdOPtL7UQeGyYCsUzRgjxC";

    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk77fA+drzJ5eIZk67XYyH/IAXxV8wJrzdE1Xhw+hze/i8Ahgj3oxQ9Ceapvs+zAN4Zs/9swkyRP92aWaSOjTmBUgLNHaazq8zza36+x9r3tLwGrOy8zRsBuuKVNoVVXKi0iMI3dOTsPbCRcdd0fTlokp5TXVF5RXexURiqJMU5BLRi1dC/2z7R8UEInqEKC1a5ersNnoY19iF6AiK8TN1VTQcr9coCmWYfybZLBEbqZpDbYWDpy1DewA0K5sVbSppMaDXWjNrOUsaEs55DG7FNFvlRMPueQBrFPgUPqkRkp2tJ6nobWSs7OwOSezmRmTTvahwFX9z9dFFYzJnAb9bwIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "http://6wdkr4.natappfree.cc/api/payments/alipay/notify";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:5173/trips";
    private AlipayClient alipayClient = null;
    public PayUtil() {
        alipayClient = new DefaultAlipayClient(
                GATEWAY_URL,
                APP_ID,
                APP_PRIVATE_KEY,
                FORMAT,
                CHARSET,
                ALIPAY_PUBLIC_KEY,
                SIGN_TYPE
        );
    }
    //支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, BigDecimal totalAmount, String subject) throws AlipayApiException {

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        //商品描述（可空）
        String body = "";
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是："+result );
        return result;
    }

    // 发起退款请求
    public AlipayTradeRefundResponse sendRefundRequestToAlipay(String outTradeNo, BigDecimal refundAmount, String refundReason) throws AlipayApiException {
        // 创建退款请求对象
        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();

        // 设置退款请求的参数
        refundRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"refund_amount\":\"" + refundAmount + "\"," +
                "\"refund_reason\":\"" + refundReason + "\"}");

        AlipayTradeRefundResponse execute = alipayClient.execute(refundRequest);

        return execute;


    }

    //  通过订单编号查询
    public String query(String id){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body=null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
}
