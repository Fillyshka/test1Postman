package ru.itmentor.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.itmentor.spring.boot_security.demo.model.Role;
import ru.itmentor.spring.boot_security.demo.model.User;
import ru.itmentor.spring.boot_security.demo.service.RoleServiceImpl;
import ru.itmentor.spring.boot_security.demo.service.UserServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminRestController {
    private final UserServiceImpl userService;
    private final RoleServiceImpl roleService;

    @Autowired
    public AdminRestController(UserServiceImpl userService, RoleServiceImpl roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsersAndRoles();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.readUser(id.intValue());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/users/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println("Received request to create user: " + user);
        try {
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                Role defaultRole = roleService.findByName("ROLE_USER")
                        .orElseGet(() -> roleService.save(new Role("ROLE_USER")));
                Set<Role> roles = new HashSet<>();
                roles.add(defaultRole);
                user.setRoles(roles);
            } else {
                Set<Role> roles = new HashSet<>();
                for (Role role : user.getRoles()) {
                    Role existingRole = roleService.findByName(role.getName())
                            .orElseGet(() -> roleService.save(new Role(role.getName())));
                    roles.add(existingRole);
                }
                user.setRoles(roles);
            }
            userService.createUser(user);
            System.out.println("User created successfully: " + user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User updatedUser) {
        try {
            User user = userService.readUser(id.intValue());
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
            }
            if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (Role role : updatedUser.getRoles()) {
                    Role existingRole = roleService.findByName(role.getName())
                            .orElseGet(() -> roleService.save(new Role(role.getName())));
                    roles.add(existingRole);
                }
                user.setRoles(roles);
            }
            userService.updateUser(id.intValue(), user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id){
        try{
            userService.deleteUser(id.intValue());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}