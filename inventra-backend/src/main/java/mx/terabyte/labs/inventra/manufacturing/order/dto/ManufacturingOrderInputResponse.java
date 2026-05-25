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
        BigDecimal actualQuantity,
        UUID inventoryMovementId,
        String movementType,
        BigDecimal stockBefore,
        BigDecimal stockAfter,
        String createdByUsername,
        LocalDateTime createdAt
) {
}