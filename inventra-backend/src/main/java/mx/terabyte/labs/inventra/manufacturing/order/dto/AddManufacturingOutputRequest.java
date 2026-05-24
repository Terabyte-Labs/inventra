package mx.terabyte.labs.inventra.manufacturing.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddManufacturingOutputRequest(

        @NotBlank
        String productSku,

        @NotNull
        @Positive
        BigDecimal quantity,

        String notes

) {
}