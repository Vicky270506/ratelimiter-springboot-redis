package com.vignesh.ratelimiter.filter;


import com.vignesh.ratelimiter.service.RateLimiterService;
import com.vignesh.ratelimiter.service.ViolationLogService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Filter intercepts every request before it hits controllers
// Allowed requests: chain.doFilter() passes them forward
// Blocked requests: write 429 directly, never call chain.doFilter()

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private ViolationLogService violationLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String ipAddress = httpRequest.getRemoteAddr();
            String endpoint = httpRequest.getRequestURI();

            //to Skip rate limiting for dashboard and static resources
            if(endpoint.startsWith("/dashboard") || (endpoint.startsWith("/actuator")) || endpoint.equals("/") ||
                endpoint.endsWith(".html")){
                chain.doFilter(request, response);
                return;
            }

            //Record start time to measure filter latency
            long startTime = System.currentTimeMillis();

            if(rateLimiterService.isAllowed(ipAddress)){
                //Request is within limit - so pass this request
                chain.doFilter(request, response);
            }
            else{
                //Request exceeded limit - log async, block immediately
                violationLogService.logViolation(ipAddress, endpoint);

                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                        "{\"error\":\"Too Many Requests\"," +
                                "\"message\":\"Rate limit exceeded. Try again later.\"}"
                );
            }

            //Log latency in MySQL - this is the proof for sub-5ms overhead
            long latency = System.currentTimeMillis() - startTime;
            System.out.println("[RateLimiter] " + ipAddress + " | " + endpoint + " | " + latency + "ms");
    }
}
