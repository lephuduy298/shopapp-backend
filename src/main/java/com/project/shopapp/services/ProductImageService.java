package com.project.shopapp.services;

import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    final private ProductImageRepository productImageRepository;

    @Transactional
//    @Override
    public void deleteProductImage(Long imageId) throws IndvalidRuntimeException {

        this.productImageRepository.deleteById(imageId);
    }

}
