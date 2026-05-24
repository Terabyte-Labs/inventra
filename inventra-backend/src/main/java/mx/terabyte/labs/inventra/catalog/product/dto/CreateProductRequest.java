package mx.terabyte.labs.inventra.catalog.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import mx.terabyte.labs.inventra.common.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull ProductType productType,
        @NotNull @PositiveOrZero BigDecimal minStock,
        @NotNull Boolean active,
        @NotBlank String unitOfMeasureCode,
        UUID categoryId
) {
}