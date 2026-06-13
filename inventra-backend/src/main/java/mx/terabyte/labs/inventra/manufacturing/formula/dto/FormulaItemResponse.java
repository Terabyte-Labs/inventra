package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FormulaItemResponse(
        UUID id,
        String productSku,
        String productName,
        String productType,
        String categoryName,
        BigDecimal quantity,
        String unitOfMeasureCode,
        String unitOfMeasureName,
        String inventoryUnitOfMeasureCode,
        String inventoryUnitOfMeasureName,
        Integer processStepNumber,
        String processStepName,
        String processStepType
) {
}
