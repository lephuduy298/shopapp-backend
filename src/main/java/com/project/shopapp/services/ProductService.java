package com.project.shopapp.services;

import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.services.iservice.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) throws PostException {
        Category exitstCategory = this.categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new PostException("Category not found"));

        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .thumbnail(productDTO.getThumbnail())
                .category(exitstCategory)
                .build();
        return this.productRepository.save(newProduct);
    }

    @Override
    public Product updateProduct(long id, ProductDTO productDTO) {
        Product currentProduct = this.getProductById(id);

        if(currentProduct != null){
            Category currentCategory = this.categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tồn tại category với id = " + productDTO.getCategoryId()));
            currentProduct.setName(productDTO.getName());
            currentProduct.setCategory(currentCategory);
            currentProduct.setPrice(productDTO.getPrice());
            currentProduct.setDescription(productDTO.getDescription());
            currentProduct.setThumbnail(productDTO.getThumbnail());

            return this.productRepository.save(currentProduct);
        }
        return null;
//        currentProduct.set
    }

    @Override
    public Product getProductById(long id) {
        return this.productRepository.findById(id).orElseThrow(() -> new RuntimeException("product not found"));
    }

    @Override
    public Page<ResProduct> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) {
        Page<Product> productPage = this.productRepository.searchProducts(keyword, categoryId, pageRequest);

//        ResultPagination result = new ResultPagination();

        return productPage.map(ResProduct::convertToResProduct);
    }

    @Override
    public void deleteProduct(long id) {
        this.productRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return this.productRepository.existsByName(name);
    }

    @Override
public ProductImage createProductImage(long productId, ProductImageDTO productImageDTO) throws IndvalidRuntimeException {
        Product existsProduct = this.getProductById(productId);
        ProductImage newProductImage = ProductImage.builder()
                .product(existsProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();

        int size = this.productImageRepository.findByProductId(productId).size();
        if (size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new IndvalidRuntimeException("Numbers of images must be <= " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return productImageRepository.save(newProductImage);
    }

    public boolean existsById(long id) {
        return this.productRepository.existsById(id);
    }

    @Override
    public List<Product> getProductsByIds(List<Long> productIds) {
        return this.productRepository.findByIdIn(productIds);
    }
}
