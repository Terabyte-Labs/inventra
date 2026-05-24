package mx.terabyte.labs.inventra.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProductCategoryRequest(

        @NotBlank
        String name,

        String description

) {
}