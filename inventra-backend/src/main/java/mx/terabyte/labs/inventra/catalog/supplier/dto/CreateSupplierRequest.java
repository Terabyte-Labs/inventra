package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateSupplierRequest(
        @NotBlank
        String code,

        @NotBlank
        String name,

        String contactName,

        @Email
        String email,

        String phone,

        Boolean active,

        List<@Valid CreateSupplierContactRequest> contacts
) {
}
