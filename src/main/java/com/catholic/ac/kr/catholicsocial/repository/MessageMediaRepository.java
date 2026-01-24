package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMediaRepository extends JpaRepository<MessageMedia, Long> {
    @Query("""
            SELECT mm
            FROM MessageMedia mm
            WHERE mm.message.id IN :messageIds
            """)
    List<MessageMedia> findAllByMessageIds(List<Long> messageIds);
}
