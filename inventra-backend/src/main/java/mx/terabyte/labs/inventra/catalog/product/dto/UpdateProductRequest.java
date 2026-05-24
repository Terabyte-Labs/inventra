package mx.terabyte.labs.inventra.catalog.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.terabyte.labs.inventra.common.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(

        @NotBlank
        String name,

        String description,

        @NotNull
        ProductType productType,

        @NotNull
        BigDecimal minStock,

        @NotNull
        Boolean active,

        @NotNull
        UUID categoryId

) {
}