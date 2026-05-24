package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompleteManufacturingOrderResponse(
        UUID manufacturingOrderId,
        String orderNumber,
        BigDecimal actualQuantity,
        ManufacturingOrderStatus status,
        LocalDateTime completedAt
) {
}