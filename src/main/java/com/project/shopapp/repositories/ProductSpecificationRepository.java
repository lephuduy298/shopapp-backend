package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    void deleteByProduct(Product product);
}
