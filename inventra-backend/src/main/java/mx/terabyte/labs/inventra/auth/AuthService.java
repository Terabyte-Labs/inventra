package mx.terabyte.labs.inventra.auth;

import lombok.RequiredArgsConstructor;
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
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(
                        "INVALID_CREDENTIALS",
                        "Invalid username or password"
                ));

        if (!Boolean.TRUE.equals(user.getActive())) {
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
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }
}