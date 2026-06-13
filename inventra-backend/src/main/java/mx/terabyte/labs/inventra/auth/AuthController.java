package mx.terabyte.labs.inventra.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.auth.dto.ChangePasswordRequest;
import mx.terabyte.labs.inventra.auth.dto.ChangePasswordResponse;
import mx.terabyte.labs.inventra.auth.dto.LoginRequest;
import mx.terabyte.labs.inventra.auth.dto.LoginResponse;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.ok(
                authService.login(request)
        );
    }

    @PostMapping("/change-password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ApiResponse.ok(
                authService.changePassword(request)
        );
    }
}
