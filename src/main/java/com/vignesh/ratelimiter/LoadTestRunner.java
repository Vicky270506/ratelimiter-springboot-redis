package com.vignesh.ratelimiter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestRunner {
    private static final String TARGET_URL = "http://localhost:8080/api/data";
    private static final int CONCURRENT_USERS = 50;

    public static void main(String[] args) throws InterruptedException{
        HttpClient client = HttpClient.newHttpClient(); //Shared Http Client

        // Counters - AtomicInteger is thread-safe, unlike regular int
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Thread Pool with exactly CONCURRENT_USERS thread
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);

        // CountDownLatch - all threads wait here until latch hits 0
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_USERS);

        System.out.println("Preparing " + CONCURRENT_USERS + " concurrent threads...");

        for(int i=0; i<CONCURRENT_USERS; i++){
            final int threadId = i+1;
            executor.submit(() -> {
                try{
                    startGate.await();// wait until all threads are ready

                    long start = System.currentTimeMillis();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(TARGET_URL))
                            .GET()
                            .build();
                    HttpResponse<String> response = client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    long latency = System.currentTimeMillis() - start;
                    int status = response.statusCode();

                    if(status == 200){
                        successCount.incrementAndGet();
                    } else if (status == 429) {
                        blockedCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }

                    System.out.println("Thread " + threadId + " → HTTP " + status + " (" + latency + "ms)");
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.out.println("Thread " + threadId + " -> ERROR: " + e.getMessage());
                } finally {
                    doneLatch.countDown(); // Signals this thread is Done
                }
            });
        }

        Thread.sleep(500);

        System.out.println("Releasing all threads simultaneously...\n");
        long testStart = System.currentTimeMillis();
        startGate.countDown();

        doneLatch.await(); // Wait for all threads to finish
        long totalTime = System.currentTimeMillis() - testStart;

        executor.shutdown();

        System.out.println("\n========== LOAD TEST RESULTS ==========");
        System.out.println("Total threads fired : " + CONCURRENT_USERS);
        System.out.println("Allowed (200)       : " + successCount.get());
        System.out.println("Blocked (429)       : " + blockedCount.get());
        System.out.println("Errors              : " + errorCount.get());
        System.out.println("Total test duration : " + totalTime + "ms");
        System.out.println("========================================");
        System.out.println("\nExpected: 10 allowed, 40 blocked");
        System.out.println("If blocked = 40, Lua atomicity is proven.");
    }
}
