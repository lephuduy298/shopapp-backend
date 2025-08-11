package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.res.ResCartItem;
import com.project.shopapp.models.Cart;
import com.project.shopapp.models.CartItem;
import java.util.List;

public interface ICartService {
    Cart getCartByUserId(Long userId);
    Cart createCart(Long userId);
    void deleteCart(Long cartId);
    CartItem addItemToCart(Long cartId, Long productId, int quantity);
    ResCartItem updateCartItem(Long cartItemId, int quantity);
    void removeCartItem(Long cartItemId);
    List<CartItem> getCartItems(Long cartId);

    Cart getCartById(Long cartId);
}
