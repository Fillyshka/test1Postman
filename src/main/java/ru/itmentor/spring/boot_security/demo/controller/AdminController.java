package ru.itmentor.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.itmentor.spring.boot_security.demo.model.Role;
import ru.itmentor.spring.boot_security.demo.model.User;
import ru.itmentor.spring.boot_security.demo.service.RoleServiceImpl;
import ru.itmentor.spring.boot_security.demo.service.UserServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserServiceImpl userService;
    private final RoleServiceImpl roleService;

    @Autowired
    public AdminController(UserServiceImpl userService, RoleServiceImpl roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    private void addAllRolesToModel(Model model) {
        List<Role> roles = roleService.findAllRoles();
        model.addAttribute("allRoles", roles);
    }

    @GetMapping("/users")
    public String showUserList(Model model) {
        List<User> users = userService.getUsersAndRoles();
        model.addAttribute("users", users);
        return "admin/all";
    }

    @GetMapping("/{id}")
    public String readUser(@PathVariable("id") int id, Model model) {
        model.addAttribute("user", userService.readUser(id));
        return "admin/show";
    }

    @GetMapping("/createUser")
    public String showCreateForm(Model model) {
        addAllRolesToModel(model);
        model.addAttribute("user", new User());
        return "admin/new";
    }

    @PostMapping
    public String createUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "roles", required = false) List<String> roleNames) {
        System.out.println("Creating user: name=" + name + ", email=" + email + ", password=" + password);
        System.out.println("Selected roles: " + roleNames);

        User user = new User(name, email, password);
        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                Role role = roleService.findByName("ROLE_" + roleName.toUpperCase())
                        .orElseGet(() -> roleService.save(new Role("ROLE_" + roleName.toUpperCase())));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleService.findByName("ROLE_USER")
                    .orElseGet(() -> roleService.save(new Role("ROLE_USER")));
            roles.add(defaultRole);
        }

        user.setRoles(roles);
        userService.createUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/editUser/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        User user = userService.readUser(id);
        List<Role> roles = roleService.findAllRoles();
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roles);
        return "admin/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(
            @PathVariable("id") int id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "roles", required = false) List<String> roleNames) {
        System.out.println("Updating user: id=" + id + ", name=" + name + ", email=" + email + ", password=" + password);
        System.out.println("Selected roles: " + roleNames);

        User user = userService.readUser(id);
        user.setName(name);
        user.setEmail(email);
        if (password != null && !password.isEmpty()) {
            user.setPassword(password);
        }

        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                Role role = roleService.findByName("ROLE_" + roleName.toUpperCase())
                        .orElseGet(() -> roleService.save(new Role("ROLE_" + roleName.toUpperCase())));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleService.findByName("ROLE_USER")
                    .orElseGet(() -> roleService.save(new Role("ROLE_USER")));
            roles.add(defaultRole);
        }

        user.setRoles(roles);
        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}