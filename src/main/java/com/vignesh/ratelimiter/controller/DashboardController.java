package com.vignesh.ratelimiter.controller;

import com.vignesh.ratelimiter.model.ViolationLog;
import com.vignesh.ratelimiter.repository.ViolationLogRepository;
import com.vignesh.ratelimiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ViolationLogRepository violationLogRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/violations")
    public List<ViolationLog> getRecentViolations(){
        return violationLogRepository.findTop100ByOrderByBlockedAtDesc();
    }

    @GetMapping("/stats/{ip}")
    public Map<String, Object> getStatsForIp(@PathVariable String ip){
        Map<String, Object> stats = new HashMap<>();
        stats.put("ipAddress", ip);
        stats.put("currentRequestCount", rateLimiterService.getCurrentCount(ip));
        stats.put("totalViolations", violationLogRepository.countByIpAddress(ip));

        return stats;
    }

    @GetMapping("/stats")
    public Map<String, Object> getOverallStats(){
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalViolations", violationLogRepository.count());
        stats.put("recentViolations", violationLogRepository.findTop100ByOrderByBlockedAtDesc());

        return stats;
    }
}
