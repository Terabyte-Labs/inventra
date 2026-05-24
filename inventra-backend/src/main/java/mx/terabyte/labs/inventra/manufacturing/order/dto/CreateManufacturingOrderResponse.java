package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateManufacturingOrderResponse(

        UUID manufacturingOrderId,

        String orderNumber,

        String formulaCode,

        Integer formulaVersion,

        String productSku,

        BigDecimal plannedQuantity,

        ManufacturingOrderStatus status

) {
}