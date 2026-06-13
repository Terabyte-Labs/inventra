package mx.terabyte.labs.inventra.auth.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.auth.CurrentUserService;
import mx.terabyte.labs.inventra.auth.dto.CreateUserRequest;
import mx.terabyte.labs.inventra.auth.dto.CreateUserResponse;
import mx.terabyte.labs.inventra.auth.role.RoleEntity;
import mx.terabyte.labs.inventra.auth.role.RoleRepository;
import mx.terabyte.labs.inventra.auth.user.dto.*;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_ROLE = "USER";
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@$%";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        assertAdmin();

        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserEntity::getUsername))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        assertAdmin();

        return toResponse(getUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findRoles() {
        assertAdmin();

        return roleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(RoleEntity::getName))
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription()
                ))
                .toList();
    }

    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        assertAdmin();

        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(
                    "USER_ALREADY_EXISTS",
                    "User already exists with username: " + username
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "User already exists with email: " + email
            );
        }

        String temporaryPassword = normalizePasswordOrGenerate(request.temporaryPassword());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
        user.setMustChangePassword(request.mustChangePassword() == null || Boolean.TRUE.equals(request.mustChangePassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastPasswordChangedAt(LocalDateTime.now());
        user.setRoles(resolveRoles(request.roleNames()));

        userRepository.save(user);

        log.info("User created: username={}, userId={}, requestId={}",
                user.getUsername(), user.getId(), MDC.get("requestId"));

        return new CreateUserResponse(
                toResponse(user),
                temporaryPassword
        );
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        assertAdmin();

        UserEntity user = getUserOrThrow(id);

        String email = request.email().trim().toLowerCase();

        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "EMAIL_ALREADY_EXISTS",
                            "User already exists with email: " + email
                    );
                });

        user.setEmail(email);
        user.setFullName(request.fullName().trim());

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.roleNames() != null) {
            user.setRoles(resolveRoles(request.roleNames()));
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("User updated: username={}, userId={}, requestId={}",
                user.getUsername(), user.getId(), MDC.get("requestId"));

        return toResponse(user);
    }

    @Transactional
    public UserResponse activate(UUID id) {
        assertAdmin();

        UserEntity user = getUserOrThrow(id);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public UserResponse deactivate(UUID id) {
        assertAdmin();

        UserEntity user = getUserOrThrow(id);
        UserEntity currentUser = currentUserService.getCurrentUser();

        if (user.getId().equals(currentUser.getId())) {
            throw new BusinessException(
                    "CANNOT_DEACTIVATE_SELF",
                    "You cannot deactivate your own user"
            );
        }

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public ResetPasswordResponse resetPassword(UUID id, ResetPasswordRequest request) {
        assertAdmin();

        UserEntity user = getUserOrThrow(id);
        String temporaryPassword = normalizePasswordOrGenerate(
                request != null ? request.temporaryPassword() : null
        );

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(request == null
                || request.mustChangePassword() == null
                || Boolean.TRUE.equals(request.mustChangePassword()));
        user.setLastPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Password reset by admin: username={}, userId={}, mustChangePassword={}, requestId={}",
                user.getUsername(), user.getId(), user.getMustChangePassword(), MDC.get("requestId"));

        return new ResetPasswordResponse(
                toResponse(user),
                temporaryPassword
        );
    }

    private UserEntity getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "User not found: " + id
                ));
    }

    private Set<RoleEntity> resolveRoles(List<String> roleNames) {
        List<String> normalizedRoles = roleNames == null || roleNames.isEmpty()
                ? List.of(DEFAULT_ROLE)
                : roleNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .distinct()
                .toList();

        Set<RoleEntity> roles = new HashSet<>();

        for (String roleName : normalizedRoles) {
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BusinessException(
                            "ROLE_NOT_FOUND",
                            "Role not found: " + roleName
                    ));

            roles.add(role);
        }

        return roles;
    }

    private String normalizePasswordOrGenerate(String password) {
        if (password != null && !password.isBlank()) {
            return password;
        }

        return generateTemporaryPassword();
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 14; i++) {
            password.append(PASSWORD_CHARS.charAt(
                    secureRandom.nextInt(PASSWORD_CHARS.length())
            ));
        }

        return password.toString();
    }

    private void assertAdmin() {
        UserEntity currentUser = currentUserService.getCurrentUser();

        boolean isAdmin = currentUser.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .anyMatch(ADMIN_ROLE::equals);

        if (!isAdmin) {
            throw new BusinessException(
                    "FORBIDDEN",
                    "Only ADMIN users can manage users"
            );
        }
    }

    private UserResponse toResponse(UserEntity user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .sorted()
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getActive(),
                user.getMustChangePassword(),
                user.getLastPasswordChangedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roles
        );
    }
}
