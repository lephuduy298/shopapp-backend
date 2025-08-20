package com.project.shopapp.services;

import com.project.shopapp.dto.ProductCommentDTO;
import com.project.shopapp.dto.res.ResProductComment;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductComment;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.ProductCommentRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IProductCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductCommentService implements IProductCommentService {
    private final ProductCommentRepository productCommentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductComment insertComment(ProductCommentDTO productCommentDTO) {
        User user = userRepository.findById(productCommentDTO.getUserId()).orElse(null);
        Product product = productRepository.findById(productCommentDTO.getProductId()).orElse(null);
        if (user == null || product == null) {
            throw new IllegalArgumentException("User or product not found");
        }
        ProductComment newComment = ProductComment.builder()
                .user(user)
                .product(product)
                .content(productCommentDTO.getContent())
                .build();
        return productCommentRepository.save(newComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        productCommentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void updateComment(Long id, ProductCommentDTO productCommentDTO) throws DataNotFoundException {
        ProductComment existingComment = productCommentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Comment not found"));
        existingComment.setContent(productCommentDTO.getContent());
        productCommentRepository.save(existingComment);
    }

    @Override
    public List<ResProductComment> getCommentsByUserAndProduct(Long userId, Long productId) {
        List<ProductComment> comments = productCommentRepository.findByUserIdAndProductId(userId, productId);
        return comments.stream()
                .map(comment -> ResProductComment.fromComment(comment))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResProductComment> getCommentsByProduct(Long productId) {
        List<ProductComment> comments = productCommentRepository.findByProductId(productId);
        return comments.stream()
                .map(comment -> ResProductComment.fromComment(comment))
                .collect(Collectors.toList());
    }

    @Override
    public ResProductComment getCommentById(Long commentId) {
        ProductComment productComment = productCommentRepository.findById(commentId).orElseThrow(() -> new DataNotFoundException("Comment not found"));

        return  ResProductComment.fromComment(productComment);
    }

}
