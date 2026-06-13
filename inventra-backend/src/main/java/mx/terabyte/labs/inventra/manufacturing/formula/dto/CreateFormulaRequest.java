package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateFormulaRequest(
        @NotBlank String code,
        @NotNull Integer version,
        @NotBlank String name,
        @NotBlank String outputProductSku,
        @NotNull @Positive BigDecimal outputQuantity,
        @NotBlank String outputUnitOfMeasureCode,
        @NotEmpty List<@Valid CreateFormulaItemRequest> items,
        @NotEmpty List<@Valid CreateFormulaProcessStepRequest> processSteps
) {
}
