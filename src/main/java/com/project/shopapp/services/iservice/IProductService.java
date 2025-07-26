package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.UpdateProductDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws PostException, IndvalidRuntimeException;

    Product updateProduct(long id, UpdateProductDTO updateProductDTO) throws IndvalidRuntimeException;

    Product getProductById(long id);

    Page<ResProduct> getAllProducts(String keyword, Long categoryId,PageRequest pageRequest);

    void deleteProduct(long id);

    boolean existsByName(String name);

    ProductImage createProductImage(long productId, ProductImageDTO productImageDTO) throws IndvalidRuntimeException;


    List<Product> getProductsByIds(List<Long> productIds);
}
