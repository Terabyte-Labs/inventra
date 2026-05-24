package mx.terabyte.labs.inventra.auth.dto;

public record LoginResponse(

        String accessToken,

        String tokenType,

        Long expiresIn

) {
}