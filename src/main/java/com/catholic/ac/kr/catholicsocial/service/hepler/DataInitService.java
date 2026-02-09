//package com.catholic.ac.kr.catholicsocial.security.systemservice;
//
//import com.catholic.ac.kr.catholicsocial.entity.model.Role;
//import com.catholic.ac.kr.catholicsocial.entity.model.User;
//import com.catholic.ac.kr.catholicsocial.entity.model.UserInfo;
//import com.catholic.ac.kr.catholicsocial.exception.ResourceNotFoundException;
//import com.catholic.ac.kr.catholicsocial.repository.RoleRepository;
//import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
//import com.catholic.ac.kr.catholicsocial.status.Sex;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class DataInitService {
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//   @PostConstruct
//    public void rolesAndAdminInit() {
//        roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role role = new Role();
//                   role.setName("ROLE_USER");
//
//                    return roleRepository.save(role);
//                });
//
//        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
//                .orElseGet(() -> {
//                    Role role = new Role();
//                    role.setName("ROLE_ADMIN");
//
//                   return roleRepository.save(role);

//    @PostConstruct
//    public void initUsersTestAPI() {
//        Role role = roleRepository.findByName("ROLE_USER")
//                .orElseThrow(() -> new ResourceNotFoundException("Role Not Found"));
//        for (int i = 0; i < 5000; i++) {
//            User user = new User();
//
//            user.setUsername("user_name" + i);
//            user.setPassword(passwordEncoder.encode(String.valueOf(i)));
//            user.setRoles(Set.of(role));
//            user.setEnabled(true);
//
//
//
//            UserInfo userInfo = new UserInfo();
//            userInfo.setFirstName("user_" + i);
//            userInfo.setLastName("test_api");
//            userInfo.setEmail("user_" + i + "@test.com");
//            userInfo.setPhone("100000000" + i);
//            userInfo.setSex(Sex.UNKNOWN);
//            userInfo.setBirthday(LocalDate.now().plusDays(i));
//
//            user.setInfo(userInfo);
//
//            userRepository.save(user);
//        }
//    }
//}
