package mx.terabyte.labs.inventra.manufacturing.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AddManufacturingInputResponse(

        UUID manufacturingOrderInputId,

        String orderNumber,

        String productSku,

        BigDecimal consumedQuantity,

        BigDecimal stockBefore,

        BigDecimal stockAfter

) {
}