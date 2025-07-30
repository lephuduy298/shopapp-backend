package com.project.shopapp.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.dto.UpdateProductDTO;
import com.project.shopapp.dto.res.ResProduct;
import com.project.shopapp.dto.res.ResultPagination;
import com.project.shopapp.dto.rest.RestResponse;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.error.StorageException;
import com.project.shopapp.models.PriceRange;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.services.FileService;
import com.project.shopapp.services.ProductService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final LocalizationUtils localizationUtils;

    private final FileService fileService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProducts(@Valid @RequestPart(value = "product") ProductDTO productDTO,
                                                     @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailImage,
                                                     @RequestParam(value = "images", required = false) List<MultipartFile> images)
            throws Exception {

        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png",
                "doc", "docx");

//        Product currentProduct = this.productService.getProductById(pro)

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            if (thumbnailImage.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(localizationUtils
                                .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
            }

            String fileName = thumbnailImage.getOriginalFilename();

            boolean isValidExtension = allowedExtensions.stream()
                    .anyMatch(item -> fileName.toLowerCase().endsWith("." + item));

            if (!isValidExtension) {
                throw new Exception("Invalid file extension. Only allow " +
                        allowedExtensions.toString());
            }

            String storeFileName = this.fileService.storeFile(thumbnailImage, "");

            productDTO.setThumbnail(storeFileName);
        }

        if (images != null && !images.isEmpty()) {

            if (productDTO.getUrls() == null) {
                productDTO.setUrls(new ArrayList<>());
            }

            for (MultipartFile image : images) {
                if (image.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                }

                String imageName = image.getOriginalFilename();
                boolean isValidImageExt = allowedExtensions.stream()
                        .anyMatch(ext -> imageName.toLowerCase().endsWith("." + ext));

                if (!isValidImageExt) {
                    throw new Exception("Invalid image file extension for file: " + imageName + ". Only allowed: " + allowedExtensions);
                }

                // Nếu cần, có thể lưu từng ảnh tại đây
                // String storedImage = this.fileService.storeFile(image, "");
                // updateProductDTO.addImage(storedImage); (nếu có list trong DTO)

                String storeFileName = this.fileService.storeFile(image, "");

                productDTO.getUrls().add(storeFileName);
            }
        }

        Product createProduct = this.productService.createProduct(productDTO);
        return ResponseEntity.ok().body(ResProduct.convertToResProduct(createProduct));

//        Product currentProduct = this.productService.createProduct(productDTO);
//
//        return ResponseEntity.ok().body(ResProduct.convertToResProduct(currentProduct));
    }

    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImage>> uploadImage(
            @PathVariable("id") long productId,
            @RequestParam("files") List<MultipartFile> files) throws StorageException, IOException, IndvalidRuntimeException {

//        List<MultipartFile> files = productDTO.getFiles();

        this.productService.getProductById(productId);


         files = files == null ? new ArrayList<MultipartFile>() : files;

        if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new StorageException(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5,
                    ProductImage.MAXIMUM_IMAGES_PER_PRODUCT));
        }

        List<ProductImage> productImages = new ArrayList<>();


        for(MultipartFile file : files){
            //kiểm tra nếu không truyền file
            if(file.getSize() == 0){
                continue;
            }

//             Kiểm tra kích thước file và định dạng
            if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
               throw new StorageException(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
            }

            //Kiểm tra định dạng file
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
            boolean isValid = allowedExtensions.stream().anyMatch(item -> file.getContentType().endsWith(item));

            if(!isValid){
                throw new StorageException(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
            }

            //Lưu file và cập nhật thumbnail vào database
            String fileName = this.fileService.storeFile(file, "");

//            Product currentProduct = this.productService.getProductById(productId);

            // tạo bảng product image
            ProductImage productImage = this.productService.createProductImage(
                    productId,
                    ProductImageDTO.builder().imageUrl(fileName).build());

            productImages.add(productImage);
        }
        return ResponseEntity.ok().body(productImages);

    }

//    private String storeFile(MultipartFile file) throws IOException, StorageException {
//        //lấy tên file gốc và dùng stringunits và cleanPath để loại bỏ ký tự thừa
//        if(file.getOriginalFilename() == null){
//            throw new StorageException("Invalid images format");
//        }
//        String filename = StringUtils.cleanPath(file.getOriginalFilename());
//        //dùng UUID để tạo tên file là duy nhất
//        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
//        Path uploadDir = Paths.get("uploads");
//
//        //kiểm tra thư mục đã tồn tại chưa và tạo
//        if (!Files.exists(uploadDir)) {
//            Files.createDirectories(uploadDir);
//        }
//
//        // Đường dẫn đầy đủ đến file
//        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
//
//        //Sao chép sang thư mục đích
//        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
//        return uniqueFilename;
//
//    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfoundimage.jpg").toUri()));
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping
//    public ResponseEntity<ResultPagination> fetchAllProducts(
//            @RequestParam(defaultValue = "") String keyword,
//            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int limit
//    ){
//
//
//        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id").ascending());
//
//        Page<ResProduct> productPage = this.productService.getAllProducts(keyword, categoryId,pageRequest);
//
//        List<ResProduct> productList = productPage.getContent();
//
//        ResultPagination result = new ResultPagination();
//        ResultPagination.Meta meta = new ResultPagination.Meta();
//
//        meta.setTotalPage(productPage.getTotalPages());
//        meta.setTotalItems(productPage.getTotalElements());
//
//        result.setMeta(meta);
//        result.setResult(productList);
//
//        return ResponseEntity.ok().body(result);
//    }

    @GetMapping
    public ResponseEntity<ResultPagination> fetchAllProducts(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0", name = "category_id", required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String brand,
            @RequestParam(name = "price_ranges", required = false) String priceRangesJson

    ) {

        List<String> brandList = new ArrayList<>();
        if (brand != null && !brand.isEmpty()) {
            brandList = Arrays.stream(brand.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        List<PriceRange> priceRangeList = new ArrayList<>();
        if (priceRangesJson != null && !priceRangesJson.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                priceRangeList = objectMapper.readValue(
                        priceRangesJson,
                        new TypeReference<List<PriceRange>>() {}
                );
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(null); // hoặc custom error response
            }
        }

        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id").ascending());

        Page<ResProduct> productPage = this.productService.getAllProducts(
                keyword,
                categoryId,
                brandList,
                priceRangeList,
                pageRequest
        );

        List<ResProduct> productList = productPage.getContent();

        ResultPagination result = new ResultPagination();
        ResultPagination.Meta meta = new ResultPagination.Meta();

        meta.setTotalPage(productPage.getTotalPages());
        meta.setTotalItems(productPage.getTotalElements());

        result.setMeta(meta);
        result.setResult(productList);

        return ResponseEntity.ok().body(result);
    }


//    @PutMapping("/{id}")
//    public ResponseEntity<ResProduct> updateProduct(@PathVariable("id") long id,@RequestBody ProductDTO productDTO){
//        Product updatedProduct = this.productService.updateProduct(id, productDTO);
//        return ResponseEntity.ok().body(ResProduct.convertToResProduct(updatedProduct));
//    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@PathVariable("id") long id,
                                                    @RequestPart(value = "product", required = false) UpdateProductDTO updateProductDTO,
                                                    @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailImage,
                                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) throws Exception {

        Product currentProduct = this.productService.getProductById(id);

        if(currentProduct != null && thumbnailImage==null){
            updateProductDTO.setThumbnail(currentProduct.getThumbnail());
        }

        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png",
                "doc", "docx");

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            if (thumbnailImage.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(localizationUtils
                                .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
            }

            String fileName = thumbnailImage.getOriginalFilename();

            boolean isValidExtension = allowedExtensions.stream()
                    .anyMatch(item -> fileName.toLowerCase().endsWith("." + item));

            if (!isValidExtension) {
                throw new Exception("Invalid file extension. Only allow " +
                        allowedExtensions.toString());
            }

            String storeFileName = this.fileService.storeFile(thumbnailImage, "");

            updateProductDTO.setThumbnail(storeFileName);
        }

        if (images != null && !images.isEmpty()) {

            if (updateProductDTO.getUrls() == null) {
                updateProductDTO.setUrls(new ArrayList<>());
            }

            for (MultipartFile image : images) {
                if (image.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                }

                String imageName = image.getOriginalFilename();
                boolean isValidImageExt = allowedExtensions.stream()
                        .anyMatch(ext -> imageName.toLowerCase().endsWith("." + ext));

                if (!isValidImageExt) {
                    throw new Exception("Invalid image file extension for file: " + imageName + ". Only allowed: " + allowedExtensions);
                }

                // Nếu cần, có thể lưu từng ảnh tại đây
                // String storedImage = this.fileService.storeFile(image, "");
                // updateProductDTO.addImage(storedImage); (nếu có list trong DTO)

                String storeFileName = this.fileService.storeFile(image, "");

                updateProductDTO.getUrls().add(storeFileName);
            }
        }

        if (updateProductDTO.getUrls() == null) {
            updateProductDTO.setUrls(new ArrayList<>());
        }

        Product updatedProduct = this.productService.updateProduct(id, updateProductDTO);
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
            throw new PostException(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_ID_NOT_EXISTS, id));
        }
        this.productService.deleteProduct(id);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY));
    }

//    @PostMapping("/generatedatafake")
    private ResponseEntity<String> generatedatafake() throws PostException, IndvalidRuntimeException {
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

    @GetMapping("/by-ids")
    public ResponseEntity<List<Product>> getProductsByIds(
            @RequestParam("ids") String ids
    ){
        //tách chuỗi thành List
        List<Long> productIds = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        List<Product> products = this.productService.getProductsByIds(productIds);
        return ResponseEntity.ok().body(products);
    }
}
