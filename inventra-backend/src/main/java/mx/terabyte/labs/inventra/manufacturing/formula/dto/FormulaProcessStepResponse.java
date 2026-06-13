package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import mx.terabyte.labs.inventra.common.enums.ManufacturingProcessStepType;

import java.util.UUID;

public record FormulaProcessStepResponse(
        UUID id,
        Integer stepNumber,
        String name,
        String description,
        ManufacturingProcessStepType stepType,
        Boolean requiresQualityCheck,
        Integer expectedDurationMinutes,
        Boolean active
) {
}
