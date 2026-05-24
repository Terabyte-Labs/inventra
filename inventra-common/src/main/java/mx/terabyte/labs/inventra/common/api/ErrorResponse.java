package mx.terabyte.labs.inventra.common.api;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(
            String code,
            String message,
            List<String> details
    ) {
        return new ErrorResponse(
                code,
                message,
                details,
                LocalDateTime.now()
        );
    }
}