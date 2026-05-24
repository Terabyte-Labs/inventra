package mx.terabyte.labs.inventra.catalog.supplier.dto;

import java.util.UUID;

public record SupplierContactResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String position,
        Boolean primaryContact,
        Boolean active
) {
}