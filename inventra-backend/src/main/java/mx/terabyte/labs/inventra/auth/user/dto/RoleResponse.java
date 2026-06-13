package mx.terabyte.labs.inventra.auth.user.dto;

import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description
) {
}
