package mx.terabyte.labs.inventra.auth.user.dto;

import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @Size(min = 8)
        String temporaryPassword,

        Boolean mustChangePassword

) {
}
