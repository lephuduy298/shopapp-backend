package com.project.shopapp.repositories;

import com.project.shopapp.models.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {
    List<ProductComment> findByUserIdAndProductId(Long userId, Long productId);

    List<ProductComment> findByProductId(Long productId);
}
