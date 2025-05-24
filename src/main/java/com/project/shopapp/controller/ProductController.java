package com.project.shopapp.controller;

import com.github.javafaker.Faker;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.dto.res.ResultPagination;
import com.project.shopapp.dto.rest.RestResponse;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.error.StorageException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.services.ProductService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ResProduct> createProducts(@Valid @RequestBody ProductDTO productDTO) throws StorageException, IOException, PostException {

        Product currentProduct = this.productService.createProduct(productDTO);

        return ResponseEntity.ok().body(ResProduct.convertToResProduct(currentProduct));
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImage>> uploadImage(
            @PathVariable("id") long id,
            @RequestParam("files") List<MultipartFile> files) throws StorageException, IOException, IndvalidRuntimeException {

//        List<MultipartFile> files = productDTO.getFiles();

         files = files == null ? new ArrayList<MultipartFile>() : files;

        if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new StorageException("You can only upload maximum " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT + " images per product");
        }

        List<ProductImage> productImages = new ArrayList<>();


        for(MultipartFile file : files){
            //kiểm tra nếu không truyền file
            if(file.getSize() == 0){
                continue;
            }

//             Kiểm tra kích thước file và định dạng
            if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
               throw new StorageException("File is too large ! Maximum size is 10MB");
            }

            //Kiểm tra định dạng file
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
            boolean isValid = allowedExtensions.stream().anyMatch(item -> file.getContentType().endsWith(item));

            if(!isValid){
                throw new StorageException("Just allow file extension: pdf, jpg, jpeg, png, doc, docx");
            }


            //Lưu file và cập nhật thumbnail vào database
            String fileName = this.storeFile(file);

            Product currentProduct = this.productService.getProductById(id);

            ProductImage productImage = this.productService.createProductImage(
                    id,
                    ProductImageDTO.builder().imageUrl(fileName).build());

            productImages.add(productImage);
        }
        return ResponseEntity.ok().body(productImages);

    }

    private String storeFile(MultipartFile file) throws IOException, StorageException {
        //lấy tên file gốc và dùng stringunits và cleanPath để loại bỏ ký tự thừa
        if(file.getOriginalFilename() == null){
            throw new StorageException("Invalid images format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        //dùng UUID để tạo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        Path uploadDir = Paths.get("uploads");

        //kiểm tra thư mục đã tồn tại chưa và tạo
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Đường dẫn đầy đủ đến file
        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);

        //Sao chép sang thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;

    }

    @GetMapping
    public ResponseEntity<ResultPagination> fetchAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){

        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt"));

        Page<ResProduct> productPage = this.productService.getAllProducts(pageRequest);

        List<ResProduct> productList = productPage.getContent();

        ResultPagination result = new ResultPagination();
        ResultPagination.Meta meta = new ResultPagination.Meta();

        meta.setTotalPage(productPage.getTotalPages());

        result.setMeta(meta);
        result.setResult(productList);

        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResProduct> updateProduct(@PathVariable("id") long id,@RequestBody ProductDTO productDTO){
        Product updatedProduct = this.productService.updateProduct(id, productDTO);
        return ResponseEntity.ok().body(ResProduct.convertToResProduct(updatedProduct));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResProduct> getProductById(@PathVariable("id") long id){
        Product currentProduct = this.productService.getProductById(id);

        return ResponseEntity.ok().body(ResProduct.convertToResProduct(currentProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") long id) throws PostException {
        boolean existsProduct = this.productService.existsById(id);
        if(!existsProduct){
            throw new PostException("id = " + id + " don't exists");
        }
        this.productService.deleteProduct(id);
        return ResponseEntity.ok().body("Delete product success");
    }

//    @PostMapping("/generatedatafake")
    private ResponseEntity<String> generatedatafake() throws PostException {
        Faker faker = new Faker();
        for(int i = 0; i < 1000; i++){
                    String productName = faker.commerce().productName();
                    if(this.productService.existsByName(productName)){
                        continue;
                    }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(3, 5))
                    .build();

                    this.productService.createProduct(productDTO);
        }
        return ResponseEntity.ok().body("Successfully generate fake data");
    }
}
