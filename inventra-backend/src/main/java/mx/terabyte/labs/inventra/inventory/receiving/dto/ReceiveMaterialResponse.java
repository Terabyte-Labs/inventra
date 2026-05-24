package mx.terabyte.labs.inventra.inventory.receiving.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceiveMaterialResponse(

        UUID movementId,

        String productSku,

        String productName,

        String lotNumber,

        BigDecimal receivedQuantity,

        BigDecimal stockBefore,

        BigDecimal stockAfter,

        String warehouseCode

) {
}