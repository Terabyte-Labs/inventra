package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateFormulaItemRequest(

        @NotBlank
        String productSku,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotBlank
        String unitOfMeasureCode,

        String notes

) {
}