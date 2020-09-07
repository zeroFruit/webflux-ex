package com.zerofruit.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/callable";

        StopWatch watch = new StopWatch();
        watch.start();

        for (int i = 0; i < 100; i += 1) {
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread {}", idx);

                StopWatch innerWatch = new StopWatch();
                innerWatch.start();

                rt.getForObject(url, String.class);

                innerWatch.stop();
                log.info("Elapsed: {} -> {}", idx, innerWatch.getTotalTimeMillis());
            });
        }

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        watch.stop();
        log.info("Total: {}", watch.getTotalTimeMillis());
    }
}
