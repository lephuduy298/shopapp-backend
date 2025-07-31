package com.project.shopapp.services.iservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.UpdateProductDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.PriceRange;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.models.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO, String specificationsJson) throws PostException, IndvalidRuntimeException, JsonProcessingException;

    Product updateProduct(long id, UpdateProductDTO updateProductDTO, List<ProductSpecification> specifications) throws IndvalidRuntimeException;

    Product getProductById(long id);

    Page<ResProduct> getAllProducts(String keyword, Long categoryId, List<String> brand, List<PriceRange> priceRanges, PageRequest pageRequest);

    void deleteProduct(long id);

    boolean existsByName(String name);

    ProductImage createProductImage(long productId, ProductImageDTO productImageDTO) throws IndvalidRuntimeException;


    List<Product> getProductsByIds(List<Long> productIds);
}
