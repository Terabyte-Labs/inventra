package mx.terabyte.labs.inventra.manufacturing.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AddManufacturingOutputResponse(

        UUID manufacturingOrderOutputId,

        String orderNumber,

        String productSku,

        String lotNumber,

        BigDecimal producedQuantity,

        BigDecimal stockBefore,

        BigDecimal stockAfter

) {
}