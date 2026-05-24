package mx.terabyte.labs.inventra.auth;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.auth.user.UserRepository;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    "UNAUTHENTICATED",
                    "Authenticated user not found"
            );
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "Authenticated user not found: " + username
                ));
    }
}