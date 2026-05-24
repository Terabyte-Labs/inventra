package mx.terabyte.labs.inventra.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("Attempting user login for username: {}", request.username());
        
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Login attempt failed: user not found for username: {}", request.username());
                    return new BusinessException(
                        "INVALID_CREDENTIALS",
                        "Invalid username or password"
                    );
                });

        if (!Boolean.TRUE.equals(user.getActive())) {
            log.warn("Login attempt failed: user account is disabled for username: {}", request.username());
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
            log.warn("Login attempt failed: invalid password for username: {}", request.username());
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(user);
        log.info("User successfully logged in: {}", request.username());

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }
}