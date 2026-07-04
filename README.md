# Distributed API Rate Limiter & Metrics Engine

A backend infrastructure middleware built with Spring Boot that sits in front of any API to throttle traffic, prevent brute-force attacks, and track usage metrics in real time.

## Architecture
Incoming Request → [Spring Boot Filter] → [Redis Lua Script Check]
↓ Blocked?
YES → 429 Response + Async MySQL Log
NO  → Pass to Controller

## Tech Stack

-**Java 21 + Spring Boot 4.0.7** - Core framework
-**Redis** - Atomic rate limiting using Lua Scripts
-**MySQL + Spring Data JPA** - Violation audit logging
-**Lombok** - Boilerplate elimination

## Core Engineering Decisions

**Why Redis?**
Rate limit checks happen on every request. MySQL would be too slow. Redis handles this in under 1ms.

**Why Lua Scripts?**
INCR and EXPIRE must run atomically. Without Lua, two simultaneous requests can both read count=9, both increment, and both get allowed - exceeding the limit. Lua scripts are atomic in Redis.

**Why @Async for logging?**
MySQL writes are slower than Redis reads. Logging violations asynchronously means the 429 response is returned immediately while the log is written in a background thread - no latency added to the blocked request.

## Rate Limitng Algorithm

Sliding Window counter using Redis INCR + EXPIRE:
-Each IP gets a Redis key with a 60-second TTL
-Every request increments the counter atomically
-Request beyond the limit receive HTTP 429
-Counter auto-resets when TTL expires

## How to Run

**Prerequisites:** Java 21, Maven, Redis, MySQL

1. Clone the repo
2. Create MySQL database:
```sql
   CREATE DATABASE ratelimiter;
```
3. Configure credentials in `application.properties`
4. Run:
```bash
   mvn spring-boot:run
```
5. Test rate limiting:
```bash
   for i in {1..15}; do curl -s -o /dev/null -w "%{http_code}\n"
   http://localhost:8080/api/data; done
```
6. View dashboard: `http://localhost:8080/index.html`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/data` | Test endpoint (rate limited) |
| POST | `/api/submit` | Test endpoint (rate limited) |
| GET | `/dashboard/stats` | Overall violation stats |
| GET | `/dashboard/violations` | Recent 100 violations |
| GET | `/dashboard/stats/{ip}` | Stats for specific IP |
