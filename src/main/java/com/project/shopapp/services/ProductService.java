package com.project.shopapp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.UpdateProductDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.ProductSpecificationRepository;
import com.project.shopapp.services.iservice.IProductService;
import com.project.shopapp.services.specification.ProductSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductSpecificationRepository productSpecificationRepository;

    @Transactional
    @Override
    public Product createProduct(ProductDTO productDTO, String specificationsJson) throws PostException, IndvalidRuntimeException, JsonProcessingException {
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

        List<ProductSpecification> specifications = new ArrayList<>();
        if (specificationsJson != null && !specificationsJson.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            specifications = Arrays.asList(objectMapper.readValue(specificationsJson, ProductSpecification[].class));
        }

        for (ProductSpecification spec : specifications) {
            this.createProductSpecification(newProduct.getId(), spec);  // currentProduct là Product đã lấy bằng id
        }

        return currentProduct;
    }

    @Transactional
    @Override
    public Product updateProduct(long id, UpdateProductDTO updateProductDTO, List<ProductSpecification> specifications) throws IndvalidRuntimeException {
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

            if (specifications != null && !specifications.isEmpty()) {
                this.productSpecificationRepository.deleteByProduct(currentProduct); // hoặc deleteAll by product
                for (ProductSpecification spec : specifications) {
                    this.createProductSpecification(id, spec);
                }
            }


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
    public Page<ResProduct> getAllProducts(String keyword, Long categoryId, List<String> brand, List<PriceRange> priceRanges, PageRequest pageRequest) {

        Specification<Product> spec = Specification
                .where(ProductSpec.hasKeyword(keyword))
                .and(ProductSpec.hasCategory(categoryId))
                .and(ProductSpec.hasBrand(brand))
                .and(ProductSpec.inPriceRanges(priceRanges));

        Page<Product> productPage = this.productRepository.findAll(spec, pageRequest);

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


    @Transactional
//    @Override
    public ProductSpecification createProductSpecification(long productId, ProductSpecification specification) throws IndvalidRuntimeException {
        Product existsProduct = this.getProductById(productId);

            ProductSpecification newProductSpecification = ProductSpecification.builder()
                    .product(existsProduct)
                    .specName(specification.getSpecName())
                    .specValue(specification.getSpecValue())
                    .build();
            return this.productSpecificationRepository.save(newProductSpecification);
    }

    public boolean existsById(long id) {
        return this.productRepository.existsById(id);
    }

    @Override
    public List<Product> getProductsByIds(List<Long> productIds) {
        return this.productRepository.findByIdIn(productIds);
    }
}
