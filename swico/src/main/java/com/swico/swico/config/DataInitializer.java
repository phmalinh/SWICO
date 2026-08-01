package com.swico.swico.config;

import com.swico.swico.entity.Role;
import com.swico.swico.entity.User;
import com.swico.swico.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        upsertUser("operator01", "operator01", "Nguyễn Văn A", Role.ROLE_OPERATOR, "A1");
        upsertUser("leader01", "leader01", "Trần Thị B", Role.ROLE_LEADER, "A1");
        upsertUser("manager01", "manager01", "Lê Văn C", Role.ROLE_MANAGER, null);
        upsertUser("admin", "admin", "Admin SWICO", Role.ROLE_ADMIN, null);
    }

    private void upsertUser(String username, String rawPassword, String fullName, Role role, String lineCode) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        boolean isNewUser = user.getId() == null;

        user.setUsername(username);
        if (isNewUser) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        user.setFullName(fullName);
        user.setRole(role);
        user.setLineCode(lineCode);
        user.setActive(true);
        userRepository.save(user);
    }
}
