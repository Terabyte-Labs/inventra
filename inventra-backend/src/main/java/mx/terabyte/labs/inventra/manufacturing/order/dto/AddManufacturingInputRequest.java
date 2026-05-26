package mx.terabyte.labs.inventra.manufacturing.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddManufacturingInputRequest(
        @NotBlank
        String productSku,

        String lotNumber,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotBlank
        String unitOfMeasureCode,

        String notes
) {
}