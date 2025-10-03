package org.example.quizzobackend.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizzobackend.auth.entity.Role;
import org.example.quizzobackend.auth.entity.User;
import org.example.quizzobackend.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Create default admin if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@quiz.com")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Administrator")
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();

                userRepository.save(admin);
                log.info("Default admin user created");
            }

            // Create test user if not exists
            if (!userRepository.existsByUsername("testuser")) {
                User user = User.builder()
                        .username("testuser")
                        .email("test@quiz.com")
                        .password(passwordEncoder.encode("test123"))
                        .fullName("Test User")
                        .role(Role.USER)
                        .enabled(true)
                        .build();

                userRepository.save(user);
                log.info("Test user created");
            }
        };
    }
}
