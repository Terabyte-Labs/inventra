package mx.terabyte.labs.inventra.catalog.product.dto;

import mx.terabyte.labs.inventra.common.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        ProductType productType,
        BigDecimal minStock,
        Boolean active
) {
}