package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Heart;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart,Long> {
    List<Heart> findByMomentId(Long momentId, Pageable pageable);

    boolean existsByUserAndMoment(User user, Moment moment);

    Optional<Heart> findByUserAndMoment(User user, Moment moment);
}
