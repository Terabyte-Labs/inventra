package mx.terabyte.labs.inventra.catalog.category.dto;

import java.util.UUID;

public record ProductCategoryResponse(
        UUID id,
        String name,
        String description
) {
}