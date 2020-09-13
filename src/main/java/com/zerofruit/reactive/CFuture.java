package com.zerofruit.reactive;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CFuture {
    public static void main(String[] args) throws InterruptedException {
        // CompletableFuture의 장점 중 하나는 CompletionStage 구현하고 있다는 것이다.
        // CompletionStage는 해당 작업이 끝났을 때 여기에 의존하고 있는 또 다른 작업을 수행할 수 있도록 API를 제공한다.
        // TODO: CompletionStage javadoc 읽어보기.
        CompletableFuture
                .runAsync(() -> log.info("runAsync"))
                .thenRunAsync(() -> log.info("thenRunAsync"));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(3, TimeUnit.SECONDS);
    }
}
