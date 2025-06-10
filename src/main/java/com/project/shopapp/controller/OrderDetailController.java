package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.dto.res.ResOrderDetail;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.services.OrderDetailService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<ResOrderDetail> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO) throws PostException {
        OrderDetail newOrderDetail = this.orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok().body(ResOrderDetail.convertToOrderDetailDTO(newOrderDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResOrderDetail> getOrderDetail(
            @Valid @PathVariable("id") Long id) throws PostException {
        OrderDetail orderDetail = this.orderDetailService.getOrderDetailById(id);

        return ResponseEntity.ok().body(ResOrderDetail.convertToOrderDetailDTO(orderDetail));
    }

    //lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ResOrderDetail>> getOrderDetails(@Valid @PathVariable("orderId") Long orderId) {
        List<OrderDetail> orderDetails = this.orderDetailService.findByOrderId(orderId);
        return ResponseEntity.ok().body(orderDetails.stream()
                .map(ResOrderDetail::convertToOrderDetailDTO).collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResOrderDetail> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO) throws PostException {

        OrderDetail orderDetail = this.orderDetailService.updateOrderDetail(id, orderDetailDTO);

        return ResponseEntity.ok().body(ResOrderDetail.convertToOrderDetailDTO(orderDetail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        this.orderDetailService.deleteById(id);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY));
    }
}
