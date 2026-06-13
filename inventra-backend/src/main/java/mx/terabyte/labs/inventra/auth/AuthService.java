package mx.terabyte.labs.inventra.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import mx.terabyte.labs.inventra.auth.dto.LoginRequest;
import mx.terabyte.labs.inventra.auth.dto.LoginResponse;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.auth.user.UserRepository;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.config.security.JwtProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {
        // requestId and username will be provided by MDC via RequestIdFilter
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
            log.warn("Login attempt failed: username={}, reason=USER_DISABLED, requestId={}", request.username(), MDC.get("requestId"));
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
            log.warn("Login failed: username={}, reason=INVALID_PASSWORD, requestId={}", request.username(), MDC.get("requestId"));
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(user);
        // Do not log token contents. Log user id and token TTL for auditing.
        log.info("User login success: username={}, userId={}, expiresInSeconds={}, requestId={}",
                request.username(), user.getId(), jwtProperties.expirationMinutes() * 60, MDC.get("requestId"));

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }
}