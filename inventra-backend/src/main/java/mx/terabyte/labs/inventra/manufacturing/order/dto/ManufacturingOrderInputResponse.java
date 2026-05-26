package mx.terabyte.labs.inventra.manufacturing.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManufacturingOrderInputResponse(
        UUID id,
        String productSku,
        String productName,
        String lotNumber,
        BigDecimal plannedQuantity,
        String plannedUnitOfMeasureCode,
        String plannedUnitOfMeasureName,
        BigDecimal actualQuantity,
        String actualUnitOfMeasureCode,
        String actualUnitOfMeasureName,
        UUID inventoryMovementId,
        String movementType,
        BigDecimal inventoryQuantity,
        String inventoryUnitOfMeasureCode,
        BigDecimal stockBefore,
        BigDecimal stockAfter,
        String createdByUsername,
        LocalDateTime createdAt
) {
}