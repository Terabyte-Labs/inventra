package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FormulaSearchResponse(
        UUID id,
        String code,
        Integer version,
        String name,
        Boolean active,

        String outputProductSku,
        String outputProductName,
        String outputProductType,
        String outputCategoryName,

        BigDecimal outputQuantity,
        String outputUnitOfMeasureCode,
        String outputUnitOfMeasureName,

        Integer itemsCount,
        Integer processStepsCount
) {
}
