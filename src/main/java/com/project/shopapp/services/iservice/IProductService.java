package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws PostException;

    Product updateProduct(long id, ProductDTO productDTO);

    Product getProductById(long id);

    Page<ResProduct> getAllProducts(PageRequest pageRequest);

    void deleteProduct(long id);

    boolean existsByName(String name);

    ProductImage createProductImage(long productId, ProductImageDTO productImageDTO) throws IndvalidRuntimeException;


}
