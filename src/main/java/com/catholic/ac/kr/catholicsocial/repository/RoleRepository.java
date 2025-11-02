package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
