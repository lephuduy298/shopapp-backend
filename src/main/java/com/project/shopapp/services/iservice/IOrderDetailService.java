package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail) throws PostException;
    OrderDetail getOrderDetailById(Long id) throws PostException;
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData) throws PostException;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);
}
