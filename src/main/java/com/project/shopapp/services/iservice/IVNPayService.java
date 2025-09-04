package com.project.shopapp.services.iservice;

import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface IVNPayService {
    String createPayment(HttpServletRequest req) throws UnsupportedEncodingException;
}
