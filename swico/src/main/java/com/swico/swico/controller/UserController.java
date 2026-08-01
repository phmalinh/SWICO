package com.swico.swico.controller;

import com.swico.swico.dto.UserResponse;
import com.swico.swico.dto.UserUpsertRequest;
import com.swico.swico.entity.Role;
import com.swico.swico.entity.User;
import com.swico.swico.repository.UserRepository;
import com.swico.swico.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserUpsertRequest request, Authentication authentication) {
        if (request.username() == null || request.username().isBlank() || request.fullName() == null || request.fullName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        if (request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(resolveRole(request.role()));
        user.setLineCode(request.lineCode());
        user.setActive(Boolean.TRUE.equals(request.active()));

        User saved = userRepository.save(user);
        auditLogService.record(
                "CREATE",
                "User",
                saved.getId(),
                authentication.getName(),
                String.format("Tạo tài khoản %s", saved.getUsername())
        );
        return ResponseEntity.ok(UserResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserUpsertRequest request, Authentication authentication) {
        return userRepository.findById(id)
                .map(existing -> {
                    if (request.username() != null && !request.username().isBlank()) {
                        existing.setUsername(request.username());
                    }
                    if (request.password() != null && !request.password().isBlank()) {
                        existing.setPassword(passwordEncoder.encode(request.password()));
                    }
                    if (request.fullName() != null && !request.fullName().isBlank()) {
                        existing.setFullName(request.fullName());
                    }
                    existing.setRole(resolveRole(request.role()));
                    existing.setLineCode(request.lineCode());
                    existing.setActive(Boolean.TRUE.equals(request.active()));
                    User saved = userRepository.save(existing);
                    auditLogService.record(
                            "UPDATE",
                            "User",
                            saved.getId(),
                            authentication.getName(),
                            String.format("Cập nhật tài khoản %s", saved.getUsername())
                    );
                    return ResponseEntity.ok(UserResponse.fromEntity(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Role resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.ROLE_OPERATOR;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ROLE_OPERATOR", "OPERATOR" -> Role.ROLE_OPERATOR;
            case "ROLE_LEADER", "LEADER" -> Role.ROLE_LEADER;
            case "ROLE_MANAGER", "MANAGER" -> Role.ROLE_MANAGER;
            case "ROLE_ADMIN", "ADMIN" -> Role.ROLE_ADMIN;
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        auditLogService.record(
                "DELETE",
                "User",
                id,
                authentication.getName(),
                String.format("Xóa tài khoản # %d", id)
        );
        return ResponseEntity.noContent().build();
    }
}
