package com.project.shopapp.services;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.services.iservice.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws PostException {

        //check exists product
        Product product = this.productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new PostException("product with id = " + orderDetailDTO.getOrderId() + " don't exists"));

        //check exists order
        Order order = this.orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new PostException("Order with id = " + orderDetailDTO.getOrderId() + " don't exists"));

        OrderDetail newOrderDetail = OrderDetail.builder()
                .price(orderDetailDTO.getPrice())
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .color(orderDetailDTO.getColor())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .build();

        newOrderDetail.setOrder(order);
//        newOrderDetail.setStatus(OrderStatus.PENDING);
        newOrderDetail.setProduct(product);

        return this.orderDetailRepository.save(newOrderDetail);
    }

    @Override
    public OrderDetail getOrderDetailById(Long id) throws PostException {
        return this.orderDetailRepository.findById(id).orElseThrow(() -> new PostException("Order detail with id = " + id + " don't exists"));
    }

    @Transactional
    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws PostException {
        OrderDetail currentOrderDetail = this.getOrderDetailById(id);

        //check exists product
        Product product = this.productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new PostException("product with id = " + orderDetailDTO.getOrderId() + " don't exists"));

        //check exists order
        Order order = this.orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new PostException("Order with id = " + orderDetailDTO.getOrderId() + " don't exists"));

        currentOrderDetail.setPrice(orderDetailDTO.getPrice());
        currentOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        currentOrderDetail.setColor(orderDetailDTO.getColor());
        currentOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        currentOrderDetail.setOrder(order);
        currentOrderDetail.setProduct(product);

        return this.orderDetailRepository.save(currentOrderDetail);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {

        //hard delete
        this.orderDetailRepository.deleteById(id);

    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return this.orderDetailRepository.findByOrderId(orderId);
    }
}
