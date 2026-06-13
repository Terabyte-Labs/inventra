package mx.terabyte.labs.inventra.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 80)
        String username,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @Size(min = 8)
        String temporaryPassword,

        Boolean active,

        Boolean mustChangePassword,

        List<String> roleNames

) {
}
