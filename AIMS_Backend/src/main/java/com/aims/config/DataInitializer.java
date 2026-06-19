package com.aims.config;

import com.aims.entity.user.User;
import com.aims.entity.user.UserRole;
import com.aims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner init() {
        return args -> {

            if (!userRepository.existsByUsername("admin")) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setEmail("admin@aims.com");
                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );
                admin.setStatus("active");

                UserRole adminRole = new UserRole();
                adminRole.setRoleId(1);
                adminRole.setRoleName("ADMIN");

                admin.getRoles().add(adminRole);

                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername("product_manager")) {

                User pm = new User();

                pm.setUsername("product_manager");
                pm.setEmail("product_manager@aims.com");
                pm.setPassword(
                        passwordEncoder.encode("product_manager123")
                );
                pm.setStatus("active");

                UserRole pmRole = new UserRole();
                pmRole.setRoleId(2);
                pmRole.setRoleName("PRODUCT_MANAGER");

                pm.getRoles().add(pmRole);

                userRepository.save(pm);
            }
        };
    }
}
