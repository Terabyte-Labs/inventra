package mx.terabyte.labs.inventra.auth.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.auth.dto.CreateUserRequest;
import mx.terabyte.labs.inventra.auth.dto.CreateUserResponse;
import mx.terabyte.labs.inventra.auth.user.dto.*;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return ApiResponse.ok(
                userService.findAll()
        );
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> findRoles() {
        return ApiResponse.ok(
                userService.findRoles()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(
            @PathVariable("id") UUID id
    ) {
        return ApiResponse.ok(
                userService.findById(id)
        );
    }

    @PostMapping
    public ApiResponse<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.ok(
                userService.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.ok(
                userService.update(id, request)
        );
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<UserResponse> activate(
            @PathVariable("id") UUID id
    ) {
        return ApiResponse.ok(
                userService.activate(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<UserResponse> deactivate(
            @PathVariable("id") UUID id
    ) {
        return ApiResponse.ok(
                userService.deactivate(id)
        );
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) ResetPasswordRequest request
    ) {
        return ApiResponse.ok(
                userService.resetPassword(id, request)
        );
    }
}
