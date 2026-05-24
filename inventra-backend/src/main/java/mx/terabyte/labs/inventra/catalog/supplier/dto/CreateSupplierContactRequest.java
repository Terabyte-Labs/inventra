package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateSupplierContactRequest(

        @NotBlank
        String name,

        @Email
        String email,

        String phone,

        String position,

        Boolean primaryContact

) {
}