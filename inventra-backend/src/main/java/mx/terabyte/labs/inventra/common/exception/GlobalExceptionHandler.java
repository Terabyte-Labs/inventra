package mx.terabyte.labs.inventra.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import mx.terabyte.labs.inventra.common.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessException(BusinessException ex) {
        log.warn("Business exception: errorCode={}, message={}, path={}, requestId={}",
                ex.getCode(), ex.getMessage(), "-", MDC.get("requestId"));

        return ErrorResponse.of(
                ex.getCode(),
                ex.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("Validation failure: errorCount={}, fields={}, requestId={}",
                details.size(), details.stream().map(d -> d.split(":")[0]).distinct().toList(), MDC.get("requestId"));

        return ErrorResponse.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                details
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: message={}, path={}, requestId={}", ex.getMessage(), request.getRequestURI(), MDC.get("requestId"), ex);
        return ErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", List.of());
    }
}