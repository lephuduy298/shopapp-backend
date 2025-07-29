package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Page<Product> findAll(Pageable pageable);//phân trang

//    @Query("SELECT p FROM Product p WHERE " +
//            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
//            "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.description LIKE %:keyword% OR p.category.name LIKE %:keyword%)")
//    Page<Product> searchProducts
//            (@Param("keyword") String keyword,
//             @Param("categoryId") Long categoryId,
//             Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN ProductSpecification ps ON ps.product = p
    WHERE 
        (:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId)
        AND (
            :keyword IS NULL OR :keyword = '' OR 
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :brands IS NULL 
            OR (
                LOWER(ps.specName) = 'hãng sản xuất' AND 
                LOWER(ps.specValue) IN :brands
            )
        )
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
""")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brands") List<String> brands,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );




    List<Product> findByIdIn(List<Long> productIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteProductImagesByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.id = :id")
    void deleteProductById(@Param("id") Long id);
}
