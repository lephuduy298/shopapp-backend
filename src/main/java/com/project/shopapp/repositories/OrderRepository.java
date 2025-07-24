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
@Query("SELECT o FROM Order o WHERE o.active = true " +
        "AND (:keyword IS NULL OR :keyword = '' OR " +
        "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(o.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(o.note) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
        "AND (:status IS NULL OR :status = '' OR o.status = :status)")
Page<Order> findAll(@Param("keyword") String keyword,
                    @Param("status") String status,
                    Pageable pageRequest);


//    @Query("SELECT o FROM Order o WHERE o.active = true AND (:keyword IS NULL OR :keyword = '' OR " +
//            "o.fullName LIKE %:keyword% " +
//            "OR o.address LIKE %:keyword% " +
//            "OR o.note LIKE %:keyword% " +
//            "OR o.email LIKE %:keyword%)")
//    Page<Order> findByKeyword(@Param("keyword") String keyword, PageRequest pageRequest);
}
