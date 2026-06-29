package com.vignesh.ratelimiter.service;

import com.vignesh.ratelimiter.model.ViolationLog;
import com.vignesh.ratelimiter.repository.ViolationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ViolationLogService {

    @Autowired
    private ViolationLogRepository repository;

    @Async
    public void logViolation(String ipAddress, String endpoint)
    {
        ViolationLog log = new ViolationLog();
        log.setIpAddress(ipAddress);
        log.setEndpoint(endpoint);
        log.setBlockedAt(LocalDateTime.now());
        repository.save(log);
    }
}
