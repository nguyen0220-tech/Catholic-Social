package com.catholic.ac.kr.catholicsocial.security.systemservice;

import com.catholic.ac.kr.catholicsocial.entity.model.Role;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.entity.model.UserInfo;
import com.catholic.ac.kr.catholicsocial.repository.RoleRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.status.Sex;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataInitService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void rolesAndAdminInit() {
        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_USER");

                    return roleRepository.save(role);
                });

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_ADMIN");

                    return roleRepository.save(role);
                });

        boolean adminNotExists = userRepository.findByUsername("admin").isEmpty();

        if (adminNotExists) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRoles(Set.of(roleAdmin));
            admin.setEnabled(true);

            UserInfo adminInfo = new UserInfo();
            adminInfo.setFirstName("admin");
            adminInfo.setLastName("admin");
            adminInfo.setEmail("catholic-social@gmail.com");
            adminInfo.setPhone("123456789");
            adminInfo.setSex(Sex.UNKNOWN);
            adminInfo.setBirthday(LocalDate.of(1990, 1, 1));

            admin.setInfo(adminInfo);

            userRepository.save(admin);

        }

    }
}
