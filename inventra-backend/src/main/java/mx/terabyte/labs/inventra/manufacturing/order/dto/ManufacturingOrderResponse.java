package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManufacturingOrderResponse(

        UUID id,

        String orderNumber,

        String formulaCode,

        Integer formulaVersion,

        String productSku,

        BigDecimal plannedQuantity,

        BigDecimal actualQuantity,

        ManufacturingOrderStatus status,

        LocalDateTime createdAt,

        LocalDateTime startedAt,

        LocalDateTime completedAt

) {
}