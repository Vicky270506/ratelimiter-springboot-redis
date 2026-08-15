package com.vignesh.ratelimiter.service;

import com.vignesh.ratelimiter.model.ViolationLog;
import com.vignesh.ratelimiter.repository.ViolationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ViolationLogService {

    @Autowired
    private ViolationLogRepository repository;

    @Async
    public void logViolation(String ipAddress, String endpoint)
    {
        ViolationLog violationLog = new ViolationLog();
        violationLog.setIpAddress(ipAddress);
        violationLog.setEndpoint(endpoint);
        violationLog.setBlockedAt(LocalDateTime.now());
        repository.save(violationLog);
        log.info("Violation logged to DB | ip = {} endpoint = {}", ipAddress, endpoint);

    }
}
