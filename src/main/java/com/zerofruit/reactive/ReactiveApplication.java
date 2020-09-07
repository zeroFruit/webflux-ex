//package com.zerofruit.reactive;
//
//import lombok.extern.slf4j.Slf4j;
//import org.reactivestreams.Publisher;
//import org.reactivestreams.Subscriber;
//import org.reactivestreams.Subscription;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
//import reactor.core.publisher.Flux;
//import reactor.core.scheduler.Schedulers;
//
//import java.time.Duration;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
//
//@Slf4j
//@SpringBootApplication
//public class ReactiveApplication {
//
//    /*
//    public static void main(String[] args) {
//        Publisher<Integer> pub = s -> s.onSubscribe(new Subscription() {
//            @Override
//            public void request(long n) {
//                log.debug("request");
//                s.onNext(1);
//                s.onNext(2);
//                s.onNext(3);
//                s.onNext(4);
//                s.onNext(5);
//                s.onComplete();
//            }
//            @Override
//            public void cancel() {
//
//            }
//        });
//
//        // create operator
//        // subscribeOn
//        Publisher<Integer> subOnPub = sub -> {
//            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
//                @Override
//                public String getThreadNamePrefix() {
//                    return "subOn-";
//                }
//            });
//            es.execute(() -> pub.subscribe(new Subscriber<>() {
//                @Override
//                public void onSubscribe(Subscription s) {
//                    sub.onSubscribe(s);
//                }
//                @Override
//                public void onNext(Integer integer) {
//                    sub.onNext(integer);
//                }
//                @Override
//                public void onError(Throwable t) {
//                    sub.onError(t);
//                    es.shutdown();
//                }
//                @Override
//                public void onComplete() {
//                    sub.onComplete();
//                    es.shutdown();
//                }
//            }));
//        };
//
//        // publishOn
//        // 하나의 스레드에서 Publisher가 Subscriber로 데이터를 전달해야한다. 여러개의 스레드에서는 전달하지 못함.
//        // 스펙이 그렇고,  그래서 newSingleThreadExecutor()는 Publisher마다 존재함.
//        Publisher<Integer> pubOnPub = sub -> subOnPub.subscribe(new Subscriber<>() {
//            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
//                @Override
//                public String getThreadNamePrefix() {
//                    return "pubOn-";
//                }
//            });
//            @Override
//            public void onSubscribe(Subscription s) {
//                sub.onSubscribe(s);
//            }
//            @Override
//            public void onNext(Integer integer) {
//                es.execute(() -> sub.onNext(integer));
//            }
//            @Override
//            public void onError(Throwable t) {
//                es.execute(() -> sub.onError(t));
//                es.shutdown();
//            }
//            @Override
//            public void onComplete() {
//                es.execute(sub::onComplete);
//                es.shutdown();
//            }
//        });
//
//        pubOnPub.subscribe(new Subscriber<>() {
//            @Override
//            public void onSubscribe(Subscription s) {
//                log.debug("onSubscribe");
//                s.request(Long.MAX_VALUE);
//            }
//            @Override
//            public void onNext(Integer integer) {
//                log.debug("onNext:{}", integer);
//            }
//            @Override
//            public void onError(Throwable t) {
//                log.debug("onError:{}", t);
//            }
//            @Override
//            public void onComplete() {
//                log.debug("onComplete");
//            }
//        });
//        log.debug("program exit");
//        // https://projectreactor.io/docs/core/release/api/
//    }
//
//     */
//
//
//    /*
//    public static void main(String[] args) {
//        /*
//        Flux.range(1, 10)
//                .log()
//                .subscribeOn(Schedulers.newSingle("sub"))
//                .subscribe();
//        log.debug("exit");
//
//
//        Publisher<Integer> pub = sub -> {
//            sub.onSubscribe(new Subscription() {
//                private int count = 1;
//                ScheduledExecutorService exec;
//                @Override
//                public void request(long n) {
//                    exec = Executors.newSingleThreadScheduledExecutor();
//                    exec.scheduleAtFixedRate(() -> {
//                        sub.onNext(count++);
//                    }, 0, 500, TimeUnit.MILLISECONDS);
//                }
//                @Override
//                public void cancel() {
//                    log.debug("cancel");
//                    exec.shutdown();
//                }
//            });
//        };
//
//        Function<Integer, Publisher<Integer>> takePub = n -> sub -> {
//            pub.subscribe(new Subscriber<>() {
//                @Override
//                public void onSubscribe(Subscription s) {
//                    sub.onSubscribe(s);
//                }
//                @Override
//                public void onNext(Integer integer) {
//                    if (integer > n) {
//                        sub.onComplete();
//                        return;
//                    }
//                    sub.onNext(integer);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    sub.onError(t);
//                }
//
//                @Override
//                public void onComplete() {
//                    sub.onComplete();
//                }
//            });
//        };
//
//        Subscriber<Integer> sub = new Subscriber<>() {
//            Subscription subscription;
//            @Override
//            public void onSubscribe(Subscription s) {
//                subscription = s;
//                log.debug("onSubscribe");
//                subscription.request(Long.MAX_VALUE);
//            }
//            @Override
//            public void onNext(Integer integer) {
//                log.debug("onNext: {}", integer);
//            }
//            @Override
//            public void onError(Throwable t) {
//
//            }
//            @Override
//            public void onComplete() {
//                log.debug("onComplete");
//                subscription.cancel();
//            }
//        };
//        takePub.apply(5).subscribe(sub);
//    }
//    */
//}
