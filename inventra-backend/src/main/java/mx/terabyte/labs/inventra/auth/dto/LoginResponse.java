package mx.terabyte.labs.inventra.auth.dto;

import java.util.List;

public record LoginResponse(

        String accessToken,

        String tokenType,

        Long expiresIn,

        String username,

        String fullName,

        List<String> roles,

        Boolean mustChangePassword

) {
}
