package mx.terabyte.labs.inventra.catalog.product.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String productTypeCode,
        String productTypeName,
        BigDecimal minStock,
        Boolean active,
        UUID categoryId,
        String categoryName,
        String unitOfMeasureCode,
        String unitOfMeasureName
) {
}