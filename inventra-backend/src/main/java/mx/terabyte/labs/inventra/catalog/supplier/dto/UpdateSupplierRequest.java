package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierRequest(

        @NotBlank
        String name,

        String contactName,

        @Email
        String email,

        String phone,

        @NotNull
        Boolean active

) {
}
