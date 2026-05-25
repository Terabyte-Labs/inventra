package mx.terabyte.labs.inventra.manufacturing.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManufacturingOrderOutputResponse(
        UUID id,
        String productSku,
        String productName,
        String lotNumber,
        BigDecimal quantity,
        UUID inventoryMovementId,
        String movementType,
        BigDecimal stockBefore,
        BigDecimal stockAfter,
        String createdByUsername,
        LocalDateTime createdAt
) {
}