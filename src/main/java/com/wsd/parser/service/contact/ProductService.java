package com.wsd.parser.service.contact;

import com.wsd.parser.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProductService {
    Boolean parseAndSave(MultipartFile file);

    Optional<ProductEntity> getProductBySku(String sku);

    Page<ProductEntity> getAllProducts(Pageable pageable);
}
