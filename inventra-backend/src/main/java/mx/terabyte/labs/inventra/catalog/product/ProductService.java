package mx.terabyte.labs.inventra.catalog.product;

import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.catalog.category.ProductCategoryEntity;
import mx.terabyte.labs.inventra.catalog.category.ProductCategoryRepository;
import mx.terabyte.labs.inventra.catalog.product.dto.CreateProductRequest;
import mx.terabyte.labs.inventra.catalog.product.dto.ProductResponse;
import mx.terabyte.labs.inventra.catalog.product.dto.ProductSearchResponse;
import mx.terabyte.labs.inventra.catalog.product.dto.UpdateProductRequest;
import mx.terabyte.labs.inventra.catalog.unit.UnitOfMeasureEntity;
import mx.terabyte.labs.inventra.catalog.unit.UnitOfMeasureRepository;
import mx.terabyte.labs.inventra.common.enums.ProductType;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductService(ProductRepository productRepository, UnitOfMeasureRepository unitOfMeasureRepository, ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        log.debug("Fetching all products without pagination");
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getProductType() != null ? product.getProductType().getCode() : null,
                product.getProductType() != null ? product.getProductType().getDisplayName() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getMinStock(),
                product.getActive(),
                product.getUnitOfMeasure().getCode(),
                product.getUnitOfMeasure().getName()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductSearchResponse> findAll(
            String sku,
            String name,
            ProductType productType,
            Boolean active,
            UUID categoryId,
            String categoryName,
            Pageable pageable
    ) {
        log.debug("Searching products with filters: sku={}, name={}, productType={}, active={}, categoryId={}", 
                sku, name, productType, active, categoryId);
        Specification<ProductEntity> spec = Specification.unrestricted();

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId)
            );
        }

        if (categoryName != null && !categoryName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("category").get("name")),
                            "%" + categoryName.toLowerCase() + "%"
                    )
            );
        }

        if (sku != null && !sku.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("sku")),
                            "%" + sku.toLowerCase() + "%"
                    )
            );
        }

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                    )
            );
        }

        if (productType != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("productType"), productType)
            );
        }

        if (active != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("active"), active)
            );
        }

        return productRepository.findAll(spec, pageable)
                .map(this::toSearchResponse);
    }

    @Transactional(readOnly = true)
    public ProductSearchResponse findBySku(String sku) {
        log.debug("Finding product by SKU: sku={}", sku);
        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Product not found for SKU: sku={}", sku);
                    return new BusinessException(
                            "PRODUCT_NOT_FOUND",
                            "Product not found for SKU: " + sku
                    );
                });

        return toSearchResponse(entity);
    }

    @Transactional
    public ProductSearchResponse update(
            String sku,
            UpdateProductRequest request
    ) {
        log.info("Updating product: sku={}, newName={}", sku, request.name());

        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.error("Product not found for update: sku={}", sku);
                    return new BusinessException(
                            "PRODUCT_NOT_FOUND",
                            "Product not found for SKU: " + sku
                    );
                });

        ProductCategoryEntity category = productCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> {
                    log.error("Product category not found: categoryId={}", request.categoryId());
                    return new BusinessException(
                            "PRODUCT_CATEGORY_NOT_FOUND",
                            "Product category not found: " + request.categoryId()
                    );
                });


        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setMinStock(request.minStock());
        entity.setActive(request.active());
        entity.setCategory(category);

        productRepository.save(entity);
        log.info("Product updated successfully: sku={}", sku);

        return toSearchResponse(entity);
    }

    @Transactional
    public ProductSearchResponse create(CreateProductRequest request) {
        log.info("Creating new product: sku={}, name={}", request.sku(), request.name());

        productRepository.findBySku(request.sku())
                .ifPresent(existing -> {
                    log.warn("Product already exists: sku={}", request.sku());
                    throw new BusinessException(
                            "PRODUCT_ALREADY_EXISTS",
                            "Product already exists for SKU: " + request.sku()
                    );
                });

        UnitOfMeasureEntity unit = unitOfMeasureRepository
                .findByCode(request.unitOfMeasureCode())
                .orElseThrow(() -> {
                    log.error("Unit of measure not found: code={}", request.unitOfMeasureCode());
                    return new BusinessException(
                            "UNIT_OF_MEASURE_NOT_FOUND",
                            "Unit of measure not found for code: " + request.unitOfMeasureCode()
                    );
                });

        ProductCategoryEntity category = productCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> {
                    log.error("Product category not found: categoryId={}", request.categoryId());
                    return new BusinessException(
                            "PRODUCT_CATEGORY_NOT_FOUND",
                            "Product category not found: " + request.categoryId()
                    );
                });

        ProductEntity entity = new ProductEntity();

        entity.setId(UUID.randomUUID());
        entity.setSku(request.sku());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setMinStock(request.minStock());
        entity.setActive(request.active());
        entity.setUnitOfMeasure(unit);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCategory(category);

        ProductEntity saved = productRepository.save(entity);
        log.info("Product created successfully: sku={}, productId={}", request.sku(), saved.getId());

        return toSearchResponse(saved);
    }

    private ProductSearchResponse toSearchResponse(ProductEntity entity) {
        return new ProductSearchResponse(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductType() != null ? entity.getProductType().getCode() : null,
                entity.getProductType() != null ? entity.getProductType().getDisplayName() : null,
                entity.getMinStock(),
                entity.getActive(),
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getCategory() != null ? entity.getCategory().getName() : null,
                entity.getUnitOfMeasure() != null ? entity.getUnitOfMeasure().getCode() : null,
                entity.getUnitOfMeasure() != null ? entity.getUnitOfMeasure().getName() : null
        );
    }

}