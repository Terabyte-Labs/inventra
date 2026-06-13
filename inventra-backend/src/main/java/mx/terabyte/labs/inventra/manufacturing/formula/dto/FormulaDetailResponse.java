package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FormulaDetailResponse(
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

        String outputInventoryUnitOfMeasureCode,
        String outputInventoryUnitOfMeasureName,

        Integer itemsCount,
        List<FormulaItemResponse> items,

        Integer processStepsCount,
        List<FormulaProcessStepResponse> processSteps,

        LocalDateTime createdAt
) {
}
