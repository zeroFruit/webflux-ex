package com.zerofruit.reactive;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@SpringBootApplication
@Slf4j
@EnableAsync
public class ReactiveSpringApplication {
    /*
    @Component
    public static class MyService {
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("hello");
        }
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveSpringApplication.class)) {
        }
    }

    @Bean
    ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(2);
        te.setMaxPoolSize(5); // queue가 꽉차야 max pool size만큼 생성
        te.setQueueCapacity(10);
        te.initialize();
        return te;
    }

    @Autowired
    MyService myService;

    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            ListenableFuture<String> f = myService.hello();
            f.addCallback(log::info, e -> log.error(e.getMessage()));
            log.info("exit");
        };
    }

     */


    @RestController
    public static class MyController {
        @GetMapping("/callable")
        public Callable<String> hello() {
            return () -> {
                log.info("callable()");
                Thread.sleep(2000);
                return "hello";
            };
//            Thread.sleep(2000);
//            return "hello";
        }

        /*
        * DeferredResult의 가장 큰 특징은 worker thread가 만들어지지 않는다.
        * 메모리에 DeferredResult를 가지고 있고 언제든지 결과를 set 해주기만한다면
        * 응답값을 해당 클라이언트에게 전달해준다.
        *
        * servlet 자원을 최소화할 수 있다.
        * */
        Queue<DeferredResult<String>> drq = new ConcurrentLinkedDeque<>();

        @GetMapping("/dr")
        public DeferredResult<String> deferredResult() {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>(600000L);
            drq.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() {
            return String.valueOf(drq.size());
        }

        @GetMapping("/dr/event")
        public String drEvent(String msg) {
            for (DeferredResult<String> dr : drq) {
                dr.setResult(String.format("Hello %s", msg));
                drq.remove(dr);
            }
            return "OK";
        }

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 1; i <= 50; i++) {
                        emitter.send(String.format("<p>Stream %d </p>", i));
                        Thread.sleep(1000);
                    }
                } catch (Exception e) { }
            });
            return emitter;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSpringApplication.class, args);
    }


}
