package mx.terabyte.labs.inventra.catalog.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.catalog.category.dto.CreateProductCategoryRequest;
import mx.terabyte.labs.inventra.catalog.category.dto.ProductCategoryResponse;
import mx.terabyte.labs.inventra.catalog.category.dto.UpdateProductCategoryRequest;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductCategoryResponse> findAll(
            String name,
            Pageable pageable
    ) {
        log.debug("Searching product categories with name filter: name={}", name);
        Specification<ProductCategoryEntity> spec = Specification.unrestricted();

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                    )
            );
        }

        return repository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ProductCategoryResponse create(CreateProductCategoryRequest request) {
        log.info("Creating new product category: name={}", request.name());
        repository.findByName(request.name())
                .ifPresent(existing -> {
                    log.warn("Product category already exists: name={}", request.name());
                    throw new BusinessException(
                            "PRODUCT_CATEGORY_ALREADY_EXISTS",
                            "Product category already exists with name: " + request.name()
                    );
                });

        ProductCategoryEntity entity = new ProductCategoryEntity();

        entity.setId(UUID.randomUUID());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCreatedAt(LocalDateTime.now());

        ProductCategoryEntity saved = repository.save(entity);
        log.info("Product category created successfully: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public ProductCategoryResponse update(
            UUID id,
            UpdateProductCategoryRequest request
    ) {
        log.info("Updating product category: id={}, newName={}", id, request.name());
        ProductCategoryEntity entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product category not found for update: id={}", id);
                    return new BusinessException(
                            "PRODUCT_CATEGORY_NOT_FOUND",
                            "Product category not found: " + id
                    );
                });

        repository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    log.warn("Product category already exists with this name: name={}", request.name());
                    throw new BusinessException(
                            "PRODUCT_CATEGORY_ALREADY_EXISTS",
                            "Product category already exists with name: " + request.name()
                    );
                });

        entity.setName(request.name());
        entity.setDescription(request.description());

        ProductCategoryEntity updated = repository.save(entity);
        log.info("Product category updated successfully: id={}", id);
        return toResponse(updated);
    }

    private ProductCategoryResponse toResponse(ProductCategoryEntity entity) {
        return new ProductCategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}