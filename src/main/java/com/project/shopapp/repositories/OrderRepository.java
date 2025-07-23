package com.project.shopapp.repositories;

import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //Tìm các đơn hàng của 1 user nào đó
    Page<Order> findAllByUserId(Long userId, Pageable pageable);


//    @Query("SELECT o FROM Order o WHERE "
//    + "(:keyword IS NULL OR :keyword = '' OR o.fullName LIKE %:keyword% OR o.address LIKE %:keyword% OR o.note LIKE  %:keyword%)")
    @Query("SELECT o FROM Order o WHERE o.active = true AND (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.note LIKE %:keyword% " +
            "OR o.phoneNumber LIKE %:keyword% " +
            "OR o.email LIKE %:keyword%)")
    Page<Order> findAll(String keyword, PageRequest pageRequest);

//    @Query("SELECT o FROM Order o WHERE o.active = true AND (:keyword IS NULL OR :keyword = '' OR " +
//            "o.fullName LIKE %:keyword% " +
//            "OR o.address LIKE %:keyword% " +
//            "OR o.note LIKE %:keyword% " +
//            "OR o.email LIKE %:keyword%)")
//    Page<Order> findByKeyword(@Param("keyword") String keyword, PageRequest pageRequest);
}
