package org.example.login.repository;

import org.example.login.domain.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refresh);

    // refresh 기준으로 token 삭제
    @Transactional
    void deleteByRefresh(String refresh);
}

