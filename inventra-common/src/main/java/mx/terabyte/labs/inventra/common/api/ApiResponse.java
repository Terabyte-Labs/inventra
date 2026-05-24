package mx.terabyte.labs.inventra.common.api;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(
                "OK",
                "Request processed successfully",
                data,
                LocalDateTime.now()
        );
    }
}