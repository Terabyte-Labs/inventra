package mx.terabyte.labs.inventra.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import mx.terabyte.labs.inventra.auth.dto.ChangePasswordRequest;
import mx.terabyte.labs.inventra.auth.dto.ChangePasswordResponse;
import mx.terabyte.labs.inventra.auth.dto.LoginRequest;
import mx.terabyte.labs.inventra.auth.dto.LoginResponse;
import mx.terabyte.labs.inventra.auth.role.RoleEntity;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.auth.user.UserRepository;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.config.security.JwtProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CurrentUserService currentUserService;

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting user login: username={}", request.username());

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Login attempt failed: user not found for username: {}", request.username());
                    return new BusinessException(
                            "INVALID_CREDENTIALS",
                            "Invalid username or password"
                    );
                });

        if (!Boolean.TRUE.equals(user.getActive())) {
            log.warn("Login attempt failed: username={}, reason=USER_DISABLED, requestId={}",
                    request.username(), MDC.get("requestId"));
            throw new BusinessException(
                    "USER_DISABLED",
                    "User is disabled"
            );
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            log.warn("Login failed: username={}, reason=INVALID_PASSWORD, requestId={}",
                    request.username(), MDC.get("requestId"));
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(user);

        List<String> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .sorted()
                .toList();

        log.info("User login success: username={}, userId={}, expiresInSeconds={}, requestId={}",
                request.username(), user.getId(), jwtProperties.expirationMinutes() * 60, MDC.get("requestId"));

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes() * 60,
                user.getUsername(),
                user.getFullName(),
                roles,
                Boolean.TRUE.equals(user.getMustChangePassword())
        );
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        UserEntity user = currentUserService.getCurrentUser();

        boolean currentPasswordMatches = passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        );

        if (!currentPasswordMatches) {
            log.warn("Password change rejected: username={}, reason=INVALID_CURRENT_PASSWORD, requestId={}",
                    user.getUsername(), MDC.get("requestId"));
            throw new BusinessException(
                    "INVALID_CURRENT_PASSWORD",
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "PASSWORD_REUSED",
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setLastPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Password changed successfully: username={}, userId={}, requestId={}",
                user.getUsername(), user.getId(), MDC.get("requestId"));

        return new ChangePasswordResponse("Password changed successfully");
    }
}
