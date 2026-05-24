package mx.terabyte.labs.inventra.inventory.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DispatchInventoryRequest(
        @NotBlank
        String sku,

        @NotBlank
        String warehouseCode,

        @NotNull
        @Positive
        BigDecimal quantity,

        String lotNumber,

        @NotBlank
        String reason,

        String notes

) {
}