package mx.terabyte.labs.inventra.inventory.warehouse.dto;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String code,
        String name,
        String location,
        Boolean active
) {
}
