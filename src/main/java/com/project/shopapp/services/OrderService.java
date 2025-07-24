package com.project.shopapp.services;

import com.project.shopapp.dto.CartItemDTO;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.res.ResOrder;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final ProductRepository productRepository;

    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    @Override
    public Order createOrder(OrderDTO orderDTO) throws IndvalidRuntimeException {

        User currentUser = this.userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() ->  new IndvalidRuntimeException("user với id = " + orderDTO.getUserId() + " không tồn tại"));

        Order newOrder = Order
                .builder()
                .fullName(orderDTO.getFullName())
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .address(orderDTO.getAddress())
                .note(orderDTO.getNote())
                .totalMoney(orderDTO.getTotalMoney())
                .shippingMethod(orderDTO.getShippingMethod())
                .shippingAddress(orderDTO.getShippingAddress())
                .paymentMethod(orderDTO.getPaymentMethod())
                .build();

        newOrder.setUser(currentUser);
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setOrderDate(LocalDate.now());

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();

        if(shippingDate.isBefore(LocalDate.now())){
            throw new IndvalidRuntimeException("Shipping date is must be at least today");
        }

//        newOrder.setShippingDate(shippingDate);
        newOrder.setActive(true);
        this.orderRepository.save(newOrder);

        //tạo danh sách OrderDetail từ cartItems
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItemDTO cartItem: orderDTO.getOrderDetails()){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(newOrder);

            Product product = this.productRepository.findById(cartItem.getProductId()).orElseThrow(() -> new DataNotFoundException("Product not found"));

            orderDetail.setProduct(product);
            orderDetail.setPrice(product.getPrice());
            orderDetail.setNumberOfProducts(cartItem.getQuantity());
//            orderDetail.setStatus(OrderStatus.PENDING);

            orderDetails.add(orderDetail);
        }

        this.orderDetailRepository.saveAll(orderDetails);

        return newOrder;
    }

    @Transactional
    @Override
    public Order updateOrder(long id, OrderDTO orderDTO) throws IndvalidRuntimeException, PostException {
        Order order = this.getOrderById(id);

        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new IndvalidRuntimeException("Cannot find user with id: " + id));
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));

        modelMapper.typeMap(CartItemDTO.class, OrderDetail.class)
                .addMappings(mapper -> mapper.skip(OrderDetail::setId));

        modelMapper.map(orderDTO, order);

        order.setUser(existingUser);
        return this.orderRepository.save(order);
    }

    @Override
    public Order getOrderById(long id) throws PostException {
        return this.orderRepository.findById(id).orElseThrow(() -> new PostException("Order with id = " + id + " not exists"));
    }

    @Override
    public Page<ResOrder> getAllOrdersByKeyWord(String keyword, String status, PageRequest pageRequest) {
        Page<Order> orderPage = this.orderRepository.findAll(keyword, status, pageRequest);

        return orderPage.map(ResOrder::convertToResOrder);
    }

    @Transactional
    @Override
    public void deleteOrderById(long id) throws PostException {
        Order order = this.getOrderById(id);
        if(order != null){
            order.setActive(false);
            this.orderRepository.save(order);
        }
    }

    @Override
    public Page<Order> getOrderByUserId(@Valid Long userId, PageRequest pageRequest) {
        return this.orderRepository.findAllByUserId(userId, pageRequest);
    }
}
