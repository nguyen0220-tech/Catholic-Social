package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Search;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.SearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {
    Page<SearchProjection> findAllByUser(User user, Pageable pageable);

    Optional<? extends Search> findByIdAndUser(Long id, User user);
}
