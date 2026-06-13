package mx.terabyte.labs.inventra.manufacturing.order.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStepStatus;
import mx.terabyte.labs.inventra.common.enums.ManufacturingProcessStepType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ManufacturingOrderStepResponse(
        UUID id,
        String orderNumber,
        Integer stepNumber,
        String name,
        String description,
        ManufacturingProcessStepType stepType,
        ManufacturingOrderStepStatus status,
        Boolean requiresQualityCheck,
        Integer expectedDurationMinutes,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String qualityResult,
        String notes
) {
}
