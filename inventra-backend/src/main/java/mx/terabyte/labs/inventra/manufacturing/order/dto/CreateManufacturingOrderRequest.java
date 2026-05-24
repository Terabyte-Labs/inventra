package mx.terabyte.labs.inventra.manufacturing.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateManufacturingOrderRequest(

        @NotBlank
        String orderNumber,

        @NotBlank
        String formulaCode,

        @NotNull
        @Positive
        Integer formulaVersion,

        @NotBlank
        String warehouseCode,

        @NotNull
        @Positive
        BigDecimal plannedQuantity

) {
}