package com.project.shopapp.controller;

import com.project.shopapp.config.VNPayConfig;
import com.project.shopapp.dto.res.ResVNPay;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.services.OrderService;
import com.project.shopapp.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;

    private final OrderService orderService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest req) throws UnsupportedEncodingException {
        String paymentUrl = vnPayService.createPayment(req);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, Object>> vnpayIpn(HttpServletRequest request) throws PostException {
        String queryString = request.getQueryString(); // raw không decode
        ResVNPay result = vnPayService.verifyVNPay(queryString, VNPayConfig.secretKey);
        Map<String, Object> res = new HashMap<>();

        if (result.isSuccess()) {
            orderService.updateOrderStatus(result.getOrderId(), OrderStatus.PROCESSING);
            res.put("RspCode", "00");
            res.put("Message", "Confirm Success");
        } else {
            orderService.updateOrderStatus(result.getOrderId(), OrderStatus.PENDING);
            res.put("RspCode", "97");
            res.put("Message", "Confirm Fail");
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(HttpServletRequest request) throws PostException {
        String queryString = request.getQueryString(); // raw không decode
        ResVNPay result = vnPayService.verifyVNPay(queryString, VNPayConfig.secretKey);

        Map<String, Object> res = new HashMap<>();
        if (result.isSuccess()) {
            orderService.updateOrderStatus(result.getOrderId(), OrderStatus.PROCESSING);
            res.put("RspCode", "00");
            res.put("Message", "Confirm Success");
        } else {
            orderService.updateOrderStatus(result.getOrderId(), OrderStatus.PENDING);
            res.put("RspCode", "97");
            res.put("Message", "Confirm Fail");
        }
        return ResponseEntity.ok(res);
    }


}
