package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierRequest(

        @NotBlank
        String name,

        @NotNull
        Boolean active

) {
}