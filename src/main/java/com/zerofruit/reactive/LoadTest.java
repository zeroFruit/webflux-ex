package com.zerofruit.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/rest?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(101);

        for (int i = 0; i < 100; i += 1) {
            es.submit(() -> {
                int idx = counter.addAndGet(1);

                barrier.await();

                log.info("Thread {}", idx);

                StopWatch innerWatch = new StopWatch();
                innerWatch.start();

                String res = rt.getForObject(url, String.class, idx);

                innerWatch.stop();
                log.info("Elapsed: {} ({} ms) / {}", idx, innerWatch.getTotalTimeMillis(), res);

                return null; // compiler가 callable로 인식하고 barrier.await()의 exception을 밖으로 던짐
            });
        }

        barrier.await();
        StopWatch watch = new StopWatch();
        watch.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        watch.stop();
        log.info("Total: {}", watch.getTotalTimeMillis());
    }
}
