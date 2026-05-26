package mx.terabyte.labs.inventra.common.api;

import java.time.LocalDateTime;
import java.util.List;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        List<String> details,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(
                "OK",
                "Request processed successfully",
                data,
                List.of(),
                LocalDateTime.now()
        );
    }

    public static ApiResponse<Void> error(
            String code,
            String message,
            List<String> details
    ) {
        return new ApiResponse<>(
                code,
                message,
                null,
                details,
                LocalDateTime.now()
        );
    }
}