package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import mx.terabyte.labs.inventra.common.enums.ManufacturingProcessStepType;

public record CreateFormulaProcessStepRequest(

        @NotNull
        @Positive
        Integer stepNumber,

        @NotBlank
        String name,

        String description,

        @NotNull
        ManufacturingProcessStepType stepType,

        @NotNull
        Boolean requiresQualityCheck,

        Integer expectedDurationMinutes

) {
}
