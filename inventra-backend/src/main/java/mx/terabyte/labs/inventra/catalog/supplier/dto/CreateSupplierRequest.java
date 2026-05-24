package mx.terabyte.labs.inventra.catalog.supplier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateSupplierRequest(
        @NotBlank
        String code,

        @NotBlank
        String name,

        List<@Valid CreateSupplierContactRequest> contacts
) {
}