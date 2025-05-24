package com.project.shopapp.services;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.res.ResOrder;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

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

        newOrder.setShippingDate(shippingDate);
        newOrder.setActive(true);
        return this.orderRepository.save(newOrder);
    }

    @Override
    public Order updateOrder(long id, OrderDTO orderDTO) throws IndvalidRuntimeException, PostException {
        Order order = this.getOrderById(id);

        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new IndvalidRuntimeException("Cannot find user with id: " + id));
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));

        modelMapper.map(orderDTO, order);
        order.setUser(existingUser);
        return this.orderRepository.save(order);
    }

    @Override
    public Order getOrderById(long id) throws PostException {
        return this.orderRepository.findById(id).orElseThrow(() -> new PostException("Order with id = " + id + " not exists"));
    }

    @Override
    public Page<ResOrder> getAllOrders(PageRequest pageRequest) {
        Page<Order> orderPage = this.orderRepository.findAll(pageRequest);

        return orderPage.map(ResOrder::convertToResOrder);
    }

    @Override
    public void deleteOrderById(long id) throws PostException {
        Order order = this.getOrderById(id);
        if(order != null){
            order.setActive(false);
            this.orderRepository.save(order);
        }
    }

    @Override
    public List<Order> getOrderByUserId(@Valid Long userId) {
        return this.orderRepository.findByUserId(userId);
    }
}
