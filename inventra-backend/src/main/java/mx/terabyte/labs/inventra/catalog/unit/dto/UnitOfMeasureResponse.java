package mx.terabyte.labs.inventra.catalog.unit.dto;

import java.util.UUID;

public record UnitOfMeasureResponse(
        UUID id,
        String code,
        String name
) {
}