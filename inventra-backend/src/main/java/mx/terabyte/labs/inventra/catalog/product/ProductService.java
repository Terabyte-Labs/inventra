package mx.terabyte.labs.inventra.catalog.product;

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
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductType(),
                entity.getMinStock(),
                entity.getActive()
        );
    }

    public Page<ProductSearchResponse> findAll(
            String sku,
            String name,
            ProductType productType,
            Boolean active,
            Pageable pageable
    ) {
        Specification<ProductEntity> spec = Specification.unrestricted();

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
        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + sku
                ));

        return toSearchResponse(entity);
    }

    @Transactional
    public ProductSearchResponse update(
            String sku,
            UpdateProductRequest request
    ) {

        ProductEntity entity = productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + sku
                ));

        ProductCategoryEntity category = productCategoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new BusinessException(
                "PRODUCT_CATEGORY_NOT_FOUND",
                "Product category not found: " + request.categoryId()
            ));


        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProductType(request.productType());
        entity.setMinStock(request.minStock());
        entity.setActive(request.active());
        entity.setCategory(category);

        productRepository.save(entity);

        return toSearchResponse(entity);
    }

    @Transactional
    public ProductSearchResponse create(CreateProductRequest request) {

        productRepository.findBySku(request.sku())
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "PRODUCT_ALREADY_EXISTS",
                            "Product already exists for SKU: " + request.sku()
                    );
                });

        UnitOfMeasureEntity unit = unitOfMeasureRepository
                .findByCode(request.unitOfMeasureCode())
                .orElseThrow(() -> new BusinessException(
                        "UNIT_OF_MEASURE_NOT_FOUND",
                        "Unit of measure not found for code: " + request.unitOfMeasureCode()
                ));

        ProductCategoryEntity category = productCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_CATEGORY_NOT_FOUND",
                        "Product category not found: " + request.categoryId()
                ));

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

        return toSearchResponse(saved);
    }

    private ProductSearchResponse toSearchResponse(ProductEntity entity) {
        return new ProductSearchResponse(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductType(),
                entity.getMinStock(),
                entity.getActive(),
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getCategory() != null ? entity.getCategory().getName() : null
        );
    }
}