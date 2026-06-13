package mx.terabyte.labs.inventra.auth.user.dto;

import mx.terabyte.labs.inventra.auth.user.UserResponse;

public record ResetPasswordResponse(
        UserResponse user,
        String temporaryPassword
) {
}
