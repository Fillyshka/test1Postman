package ru.itmentor.spring.boot_security.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import ru.itmentor.spring.boot_security.demo.model.Role;
import ru.itmentor.spring.boot_security.demo.model.User;
import ru.itmentor.spring.boot_security.demo.repository.RoleRepository;
import ru.itmentor.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SpringBootSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
	}

	@Bean
	@Transactional
	public CommandLineRunner demoData(UserService userService, RoleRepository roleRepository) {
		return args -> {
			Role adminRole = roleRepository.findByName("ROLE_ADMIN")
					.orElseGet(() -> {
						Role newRole = new Role("ROLE_ADMIN");
						return roleRepository.save(newRole);
					});

			Role userRole = roleRepository.findByName("ROLE_USER")
					.orElseGet(() -> {
						Role newRole = new Role("ROLE_USER");
						return roleRepository.save(newRole);
					});

			if (userService.findByEmail("admin@example.com") == null) {
				User admin = new User("Admin", "admin@example.com", "admin");
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				adminRoles.add(userRole);
				admin.setRoles(adminRoles);
				userService.createUser(admin);
			}

			if (userService.findByEmail("user@example.com") == null) {
				User user = new User("User", "user@example.com", "user");
				Set<Role> userRoles = new HashSet<>();
				userRoles.add(userRole);
				user.setRoles(userRoles);
				userService.createUser(user);
			}
		};
	}
}