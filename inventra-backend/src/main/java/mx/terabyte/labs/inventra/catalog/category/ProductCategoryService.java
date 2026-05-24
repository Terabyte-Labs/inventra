package mx.terabyte.labs.inventra.catalog.category;

import lombok.RequiredArgsConstructor;
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
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductCategoryResponse> findAll(
            String name,
            Pageable pageable
    ) {
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
        repository.findByName(request.name())
                .ifPresent(existing -> {
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

        return toResponse(repository.save(entity));
    }

    @Transactional
    public ProductCategoryResponse update(
            UUID id,
            UpdateProductCategoryRequest request
    ) {
        ProductCategoryEntity entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_CATEGORY_NOT_FOUND",
                        "Product category not found: " + id
                ));

        repository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "PRODUCT_CATEGORY_ALREADY_EXISTS",
                            "Product category already exists with name: " + request.name()
                    );
                });

        entity.setName(request.name());
        entity.setDescription(request.description());

        return toResponse(repository.save(entity));
    }

    private ProductCategoryResponse toResponse(ProductCategoryEntity entity) {
        return new ProductCategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}