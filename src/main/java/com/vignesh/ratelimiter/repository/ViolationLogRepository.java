package com.vignesh.ratelimiter.repository;

import com.vignesh.ratelimiter.model.ViolationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViolationLogRepository extends JpaRepository<ViolationLog, Long> {

    List<ViolationLog> findTop100ByOrderByBlockedAtDesc();
    long countByIpAddress(String ipAddress);
}
