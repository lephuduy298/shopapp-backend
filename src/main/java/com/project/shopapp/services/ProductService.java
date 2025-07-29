package com.project.shopapp.services;

import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.UpdateProductDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    @Transactional
    @Override
    public Product createProduct(ProductDTO productDTO) throws PostException, IndvalidRuntimeException {
        Category exitstCategory = this.categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new PostException("Category not found"));



        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .thumbnail(productDTO.getThumbnail())
                .category(exitstCategory)
                .build();

        Product currentProduct = this.productRepository.save(newProduct);

        for(String url: productDTO.getUrls()) {
            ProductImageDTO productImageDTO = new ProductImageDTO(currentProduct.getId(), url);
            this.createProductImage(currentProduct.getId(), productImageDTO);
        }

        return currentProduct;
    }

    @Transactional
    @Override
    public Product updateProduct(long id, UpdateProductDTO updateProductDTO) throws IndvalidRuntimeException {
        Product currentProduct = this.getProductById(id);

        if(currentProduct != null){
            if(updateProductDTO.getUrls() != null) {
                for (String url : updateProductDTO.getUrls()) {
                    ProductImageDTO productImageDTO = new ProductImageDTO(id, url);
                    this.createProductImage(id, productImageDTO);
                }
            }
        }

        if(currentProduct != null){
            Category currentCategory = this.categoryRepository.findById(updateProductDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tồn tại category với id = " + updateProductDTO.getCategoryId()));
            currentProduct.setName(updateProductDTO.getName());
            currentProduct.setCategory(currentCategory);
            currentProduct.setPrice(updateProductDTO.getPrice());
            currentProduct.setDescription(updateProductDTO.getDescription());
            currentProduct.setThumbnail(updateProductDTO.getThumbnail());

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
    public Page<ResProduct> getAllProducts(String keyword, Long categoryId, List<String> brand, Double minPrice, Double maxPrice, PageRequest pageRequest) {

        if (brand != null && brand.isEmpty()) {
            brand = null;
        }
        Page<Product> productPage = this.productRepository.searchProducts(keyword, categoryId, brand, minPrice, maxPrice, pageRequest);

        return productPage.map(ResProduct::convertToResProduct);
    }


    @Transactional
    @Override
    public void deleteProduct(long id) {
        productRepository.deleteProductImagesByProductId(id); // Xóa ảnh trước
        productRepository.deleteProductById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return this.productRepository.existsByName(name);
    }

    @Transactional
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
