package com.project.shopapp.repositories;

import com.project.shopapp.models.Cart;
import com.project.shopapp.models.CartItem;
import com.project.shopapp.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    CartItem findByCartAndProduct(Cart cart, Product product);
}
