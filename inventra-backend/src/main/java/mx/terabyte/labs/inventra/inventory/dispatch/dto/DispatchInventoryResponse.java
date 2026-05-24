package mx.terabyte.labs.inventra.inventory.dispatch.dto;

import mx.terabyte.labs.inventra.common.enums.MovementType;

import java.math.BigDecimal;
import java.util.UUID;

public record DispatchInventoryResponse(

        UUID movementId,

        String productSku,

        String productName,

        String warehouseCode,

        MovementType movementType,

        BigDecimal dispatchedQuantity,

        BigDecimal stockBefore,

        BigDecimal stockAfter,

        String reason

) {
}