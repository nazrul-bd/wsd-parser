package com.wsd.parser.service.impl;

import com.wsd.parser.entity.ProductEntity;
import com.wsd.parser.repository.ProductRepository;
import com.wsd.parser.service.contact.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer BATCH_SIZE;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, dontRollbackOn = {DataIntegrityViolationException.class})
    public Boolean parseAndSave(MultipartFile file) {

        List<ProductEntity> products = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                ProductEntity product = new ProductEntity();
                addOrUpdateProducts(products, row, product);

                if (products.size() % BATCH_SIZE == 0) {
                    productRepository.saveAllAndFlush(products); // batch of 1000 rows will persist and flush at a time
                    entityManager.clear();
                    products.clear();
                }
            }
            if (!products.isEmpty()) {
                productRepository.saveAllAndFlush(products);
            }

        } catch (IOException e) {
            log.error("Failed to parse and save products from Excel file", e);
            return false;
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate Entry Found", e);
        } catch (Exception e) {
            log.error("Unknown Error", e);
            return false;
        }
        return true;
    }

    private void addOrUpdateProducts(List<ProductEntity> products, Row row, ProductEntity product) {
        productRepository.findBySku(row.getCell(0).getStringCellValue()).ifPresentOrElse(existProd -> {
            existProd.setSku(row.getCell(0).getStringCellValue());
            existProd.setTitle(row.getCell(1).getStringCellValue());
            existProd.setPrice(valueOf(row.getCell(2).getNumericCellValue()));
            existProd.setQuantity((int) row.getCell(3).getNumericCellValue());
            existProd.setUpdated(LocalDateTime.now());
            products.add(product);
        }, () -> {
            if (row != null) {
                product.setSku(row.getCell(0).getStringCellValue());
                product.setTitle(row.getCell(1).getStringCellValue());
                product.setPrice(valueOf(row.getCell(2).getNumericCellValue()));
                product.setQuantity((int) row.getCell(3).getNumericCellValue());
                products.add(product);
            }
        });
    }

    @Override
    public Optional<ProductEntity> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public Page<ProductEntity> getAllProducts(Pageable page) {
        return productRepository.findAll(page);
    }
}
