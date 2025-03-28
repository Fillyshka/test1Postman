package ru.itmentor.spring.boot_security.demo.service;
import ru.itmentor.spring.boot_security.demo.model.Role;

import java.util.List;
import java.util.Optional;

public interface RollService {
    List<Role> findAllRoles();

    Optional<Role> findByName(String name);

    Role save(Role role);
}
