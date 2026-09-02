package com.adam.event_platform.config;

import com.adam.event_platform.model.User;
import com.adam.event_platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (userRepository.findAll().stream().noneMatch(u -> u.getRoles().contains("ROLE_ADMIN"))) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.getRoles().add("ROLE_USER"); // Start as user
                admin.getRoles().add("ROLE_ADMIN"); // Make them admin immediately for testing setup
                userRepository.save(admin);
                System.out.println(">>> SEED DATA: Created default Admin (username: admin, password: admin123)");
            }
        };
    }
}