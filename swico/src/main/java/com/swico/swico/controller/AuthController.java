package com.swico.swico.controller;

import com.swico.swico.dto.AuthResponse;
import com.swico.swico.dto.LoginRequest;
import com.swico.swico.entity.User;
import com.swico.swico.repository.UserRepository;
import com.swico.swico.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            String token = tokenProvider.generateToken(authentication);
            User user = userRepository.findByUsername(request.username()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getFullName(), user.getRole().name()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<com.swico.swico.dto.UserProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(com.swico.swico.dto.UserProfileResponse.fromEntity(user));
    }

    @PutMapping("/me")
    public ResponseEntity<com.swico.swico.dto.UserProfileResponse> updateProfile(@Valid @RequestBody com.swico.swico.dto.UpdateProfileRequest request,
                                                                                 Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setFullName(request.fullName());
        user.setLineCode(request.lineCode());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(com.swico.swico.dto.UserProfileResponse.fromEntity(saved));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody com.swico.swico.dto.ChangePasswordRequest request,
                                               Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}
