package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierContactRequest(

        @NotBlank
        String name,

        @Email
        String email,

        String phone,

        String position,

        @NotNull
        Boolean primaryContact,

        @NotNull
        Boolean active

) {
}