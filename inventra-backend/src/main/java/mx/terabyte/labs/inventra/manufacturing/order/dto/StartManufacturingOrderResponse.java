package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StartManufacturingOrderResponse(
        UUID manufacturingOrderId,
        String orderNumber,
        ManufacturingOrderStatus status,
        LocalDateTime startedAt
) {
}