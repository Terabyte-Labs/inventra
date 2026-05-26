package mx.terabyte.labs.inventra.inventory.receiving.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReceiveMaterialRequest(

        @NotBlank
        String sku,

        @NotBlank
        String supplierCode,

        @NotBlank
        String lotNumber,

        String serialNumber,

        @NotBlank
        String warehouseCode,

        @NotBlank
        String storageLocation,

        String barcode,

        @NotNull
        @Positive
        BigDecimal unitPrice,

        @NotNull
        @Positive
        BigDecimal quantity,

        @Positive
        BigDecimal presentationQuantity,

        String presentationUnit,

        LocalDateTime receivedAt,

        String notes

) {
}