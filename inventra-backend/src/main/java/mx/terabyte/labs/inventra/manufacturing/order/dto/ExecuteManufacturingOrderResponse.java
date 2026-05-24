package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ExecuteManufacturingOrderResponse(
        UUID manufacturingOrderId,
        String orderNumber,
        String productSku,
        BigDecimal producedQuantity,
        ManufacturingOrderStatus status,
        Integer consumedItems
) {
}