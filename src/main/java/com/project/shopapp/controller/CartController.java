package com.project.shopapp.controller;

import com.project.shopapp.dto.res.ResCart;
import com.project.shopapp.dto.res.ResCartItem;
import com.project.shopapp.models.Cart;
import com.project.shopapp.models.CartItem;
import com.project.shopapp.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResCart> getCartByUserId(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        ResCart resCart = cartService.mapToResCart(cart);
        return ResponseEntity.ok(resCart);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ResCart> createCart(@PathVariable Long userId) {
        Cart cart = cartService.createCart(userId);
        ResCart resCart = cartService.mapToResCart(cart);
        return ResponseEntity.ok(resCart);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("items/{userId}")
    public ResponseEntity<ResCart> addItemToCart(@PathVariable Long userId, @RequestParam Long productId, @RequestParam int quantity) {
        cartService.addItemToCart(userId, productId, quantity);
        Cart cart = cartService.getCartByUserId(userId);
        ResCart resCart = cartService.mapToResCart(cart);
        return ResponseEntity.ok(resCart);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ResCartItem> updateCartItem(@PathVariable Long cartItemId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(cartItemId, quantity));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{cartId}")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartItems(cartId));
    }
}
