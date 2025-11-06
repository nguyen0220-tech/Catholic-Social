package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    List<Moment> findByUserId(Long userId, Pageable pageable);

    Optional<Moment> findByIdAndUser(Long id, User user);
}
