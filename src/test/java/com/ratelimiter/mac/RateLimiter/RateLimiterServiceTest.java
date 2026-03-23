package com.ratelimiter.mac.RateLimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ratelimiter.service.RateLimiterService;

@SpringBootTest
public class RateLimiterServiceTest {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    public void testConcurrentSlidingWindow_MitigatesBurstTraffic() throws InterruptedException {
        String testUserId = "stress_test_user_999";
        
        // We will fire 1,000 total requests
        int totalRequests = 1000;
        // But we will only use 100 OS threads to do it (safe for your Mac)
        int maxConcurrentThreads = 100; 
        
        int expectedAllowed = 10; 
        
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentThreads);
        CountDownLatch finished = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);

        System.out.println("🚀 Firing " + totalRequests + " requests to Upstash...");

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    boolean isAllowed = rateLimiterService.isAllowed(testUserId);
                    if (isAllowed) {
                        successCount.incrementAndGet();
                    } else {
                        blockedCount.incrementAndGet();
                    }
                } finally {
                    finished.countDown();
                }
            });
        }

        // Wait for all 1000 requests to finish
        finished.await(); 
        executor.shutdown();

        System.out.println("✅ Allowed Requests: " + successCount.get());
        System.out.println("⛔ Blocked Requests: " + blockedCount.get());

        // We expect exactly 10 to pass, and 990 to be blocked!
        assertEquals(expectedAllowed, successCount.get(), "Allowed count mismatch!");
        assertEquals(totalRequests - expectedAllowed, blockedCount.get(), "Blocked count mismatch!");
    }
}