package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.res.ResOrder;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.dto.res.ResultPagination;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Order;
import com.project.shopapp.services.OrderService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<ResOrder> createOrder(@Valid @RequestBody OrderDTO orderDTO) throws IndvalidRuntimeException {
        Order order = this.orderService.createOrder(orderDTO);

        return ResponseEntity.ok().body(ResOrder.convertToResOrder(order));
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<ResultPagination> getOrders(
            @Valid @PathVariable("user_id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit){

        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id").descending());

        Page<Order> orders = this.orderService.getOrderByUserId(userId, pageRequest);

        ResultPagination result = new ResultPagination();
        ResultPagination.Meta meta = new ResultPagination.Meta();

        meta.setTotalPage(orders.getTotalPages());
        meta.setTotalItems(orders.getTotalElements());
//        meta.setTotalItems(orders.get);

        result.setMeta(meta);
//        result.setResult(orders.getContent());

        List<ResOrder> resOrders = orders.getContent().stream().map(order -> ResOrder.convertToResOrder(order)).collect(Collectors.toList());
        result.setResult(resOrders);

        return ResponseEntity.ok().body(result);
    }

//    @GetMapping
//    public ResponseEntity<ResultPagination> fetchAllProducts(
//            @RequestParam(defaultValue = "") String keyword,
//            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int limit
//    ){
//
//
//        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id").ascending());
//
//        Page<ResProduct> productPage = this.productService.getAllProducts(keyword, categoryId,pageRequest);
//
//        List<ResProduct> productList = productPage.getContent();
//
//        ResultPagination result = new ResultPagination();
//        ResultPagination.Meta meta = new ResultPagination.Meta();
//
//        meta.setTotalPage(productPage.getTotalPages());
//
//        result.setMeta(meta);
//        result.setResult(productList);
//
//        return ResponseEntity.ok().body(result);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ResOrder> getOrderById(@PathVariable("id") long id) throws PostException {
        Order order = this.orderService.getOrderById(id);
        return ResponseEntity.ok().body(ResOrder.convertToResOrder(order));
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResultPagination> getAllOrders(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "", required = false) String status
    ){
        //
        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id"));

        Page<ResOrder> orders = this.orderService.getAllOrdersByKeyWord(keyword, status, pageRequest);

        ResultPagination result = new ResultPagination();
        ResultPagination.Meta meta = new ResultPagination.Meta();

        List<ResOrder> resOrders = orders.getContent();
        meta.setTotalPage(orders.getTotalPages());
        meta.setTotalItems(orders.getTotalElements());
        result.setMeta(meta);
        result.setResult(resOrders);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResOrder> updateOrders(@Valid @PathVariable("id") Long id, @RequestBody OrderDTO orderDTO) throws IndvalidRuntimeException, PostException {
        Order order = this.orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok().body(ResOrder.convertToResOrder(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrders(@PathVariable("id") Long id) throws PostException {
        this.orderService.deleteOrderById(id);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY));
    }
}
