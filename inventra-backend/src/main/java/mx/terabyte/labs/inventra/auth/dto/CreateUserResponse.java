package mx.terabyte.labs.inventra.auth.dto;

import mx.terabyte.labs.inventra.auth.user.UserResponse;

public record CreateUserResponse(
        UserResponse user,
        String temporaryPassword
) {
}
