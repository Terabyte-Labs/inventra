package mx.terabyte.labs.inventra.inventory.movement.dto;

import mx.terabyte.labs.inventra.common.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryMovementResponse(
        UUID id,
        String productSku,
        String productName,
        String lotNumber,
        String warehouseCode,
        MovementType movementType,
        BigDecimal quantity,
        BigDecimal beforeQuantity,
        BigDecimal afterQuantity,
        BigDecimal unitPrice,
        String storageLocation,
        String notes,
        LocalDateTime createdAt
) {
}