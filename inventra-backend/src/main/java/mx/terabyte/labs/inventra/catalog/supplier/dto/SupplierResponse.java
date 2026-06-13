package mx.terabyte.labs.inventra.catalog.supplier.dto;

import java.util.List;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String code,
        String name,
        String contactName,
        String email,
        String phone,
        Boolean active,
        List<SupplierContactResponse> contacts
) {
}
