package mx.terabyte.labs.inventra.auth.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        Boolean active,
        Boolean mustChangePassword,
        LocalDateTime lastPasswordChangedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> roles
) {
}
