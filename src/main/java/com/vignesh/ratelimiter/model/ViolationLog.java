package com.vignesh.ratelimiter.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "violation_logs")
public class ViolationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

}
