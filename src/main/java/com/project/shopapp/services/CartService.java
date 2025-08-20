package com.project.shopapp.services;

import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.models.*;
import com.project.shopapp.services.iservice.ICartService;
import com.project.shopapp.repositories.CartRepository;
import com.project.shopapp.repositories.CartItemRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.dto.res.ResCart;
import com.project.shopapp.dto.res.ResCartItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Cart createCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Cart existingCart = cartRepository.findByUserId(userId);
        if (existingCart != null) {
            return existingCart;
        }
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new IllegalArgumentException("Cart not found with id: " + cartId);
        }
        cartRepository.deleteById(cartId);
    }

    @Override
    @Transactional
    public CartItem addItemToCart(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = createCart(userId);
        }
        Product product = productRepository.findById(productId).orElseThrow();
        // Sử dụng repository để kiểm tra cartItem đã tồn tại với cart và product chưa
        CartItem existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            return cartItemRepository.save(existingCartItem);
        }
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public ResCartItem updateCartItem(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        cartItem.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(cartItem);
        return mapToResCartItem(saved);
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public Cart getCartById(Long cartId) {
        return this.cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
    }

    public ResCart mapToResCart(Cart cart) {
        return ResCart.builder()
                .id(cart.getId())
                .items(cart.getItems() == null ? List.of() : cart.getItems().stream().map(item -> ResCartItem.builder()
                        .id(item.getId())
                        .product(mapProductToDTO(item.getProduct()))
                        .quantity(item.getQuantity())
                        .build())
                        .collect(Collectors.toList()))
                .userId(cart.getUser().getId())
                .build();
    }

    public ResCartItem mapToResCartItem(CartItem cartItem) {
        return ResCartItem.builder()
                .id(cartItem.getId())
                .product(mapProductToDTO(cartItem.getProduct()))
                .quantity(cartItem.getQuantity())
                .build();
    }

    private ProductDTO mapProductToDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .thumbnail(product.getThumbnail())
                .price(product.getPrice())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .build();
        // Lấy danh sách url từ ProductImage
        if (product.getProductImages() != null) {
            dto.setUrls(product.getProductImages().stream().map(ProductImage::getImageUrl).toList());
        }
        return dto;
    }
}
