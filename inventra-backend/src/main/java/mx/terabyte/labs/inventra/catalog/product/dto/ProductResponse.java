package mx.terabyte.labs.inventra.catalog.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String productTypeCode,
        String productTypeName,
        String categoryName,
        BigDecimal minStock,
        Boolean active,
        String unitOfMeasureCode,
        String unitOfMeasureName
) {
}