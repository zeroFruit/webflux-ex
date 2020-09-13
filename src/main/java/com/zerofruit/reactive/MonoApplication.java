package com.zerofruit.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@Slf4j
public class MonoApplication {

    @GetMapping("/")
    public Mono<String> hello() {
        // Publisher -> (Publisher) -> ... -> Subscriber
        //
        // - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        // - request(unbounded)
        // - onNext(Hello Webflux)
        // - onComplete()
        //
        // onSubscribe()는 Webflux 프레임워크에서 개발자가 Publisher를 만들면 내부적응로 subscribe를 걸어준다.
        // 그리고 request를 통해서 Publisher로부터 데이터를 받아온다.
        // return Mono
        //        .just("Hello Webflux")
        //        .log(); // 중간에 존재하는 Publisher로 생각할 수 있다.

        // - pos1
        // - pos2
        // - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        // - request(unbounded)
        // - Hello Webflux
        // - onNext(Hello Webflux)
        // - onComplete()
        //
        // Controller의 코드들이 실행되고나서 Publisher의 코드들이 실행된다.
        // doOnNext()의 로그는 request()를 요청하고나서 onNext()를 호출할 때 실행된다.
        //
        // 별도의 설정을 하지 않으면 컨트롤러의 코드와 Mono의 코드는 같은 스레드에서 실행되기 때문에
        // 동기적으로 실행된다. Mono를 만드는 것이 동기적으로 실행된다고 하더라도 실행되는 시점은
        // Webflux 프레임워크에서 해당 Mono를 subscribe할 때 비로소 실행된다.
        //
        // log.info("pos1");
        // Mono<String> m = Mono
        //        .just("Hello Webflux")
        //        .doOnNext(log::info)
        //        .log(); // 중간에 존재하는 Publisher로 생각할 수 있다.
        // log.info("pos2");
        // return m;

        // 아래와같이 Mono.just()를 통해서 myService.findById()를 호출하면 이는 Subscriber가
        // subscribe 하기 전에 실행되어 값을 미리 Publisher가 준비한다.
        // 따라서 해당코드는 Subscriber가 subscribe 하기 전에 실행된다.
        //
        // log.info("pos1");
        // Mono<String> m = Mono
        //        .just(myService.findById(1))
        //        .doOnNext(log::info)
        //        .log(); // 중간에 존재하는 Publisher로 생각할 수 있다.
        // log.info("pos2");
        // return m;

        // - pos1
        // - pos2
        // - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        // - request(unbounded)
        // - generateMessage()
        // - Hello Webflux
        // - onNext(Hello Webflux)
        // - onComplete()
        //
        // 아래와같이 fromSupplier를 통해 인자를 전달하면 이는 Susbscriber가 subscribe하는 시점에
        // 실행된다.
        // Controller의 코드와 스레드는 같지만 코드 실행은 비동기적으로 실행된다.
        //
        // log.info("pos1");
        // Mono<String> m = Mono
        //        .fromSupplier(MonoApplication::generateMessage)
        //        .doOnNext(log::info)
        //        .log(); // 중간에 존재하는 Publisher로 생각할 수 있다.
        // log.info("pos2");
        // return m;

        // 중간에 subscribe()를 호출하면 컨트롤러 코드 내에서 Mono의 Publish 코드가 모두 동작한 다음
        // 다음으로 넘어간다. (log.info("pos2")) 그리고 Webflux 프레임워크가 한번 더 subscribe()
        // 하여 Mono의 코드가 한번 더 동작한다.
        // Publisher는 여러 명의 Subscriber를 가질 수 있다.
        // 아래와 같이 데이터가 고정되어있는 경우를 Cold-Type의 DataSource라고 볼 수 있는데 이럴 경우
        // Subscriber는 데이터를 준비하는 코드를 처음부터 실행한다.
        // Hot-Source의 경우는 데이터베이스와 같이 저장되어있는 데이터가 아니라 실시간으로 전달되는 데이터인데
        // 이런 경우 처음부터 데이터를 전달하는 것이 아니라 이전에 전달한 데이터 이후부터 전달함.
         log.info("pos1");
         Mono<String> m = Mono
                .fromSupplier(MonoApplication::generateMessage)
                .doOnNext(log::info)
                .log(); // 중간에 존재하는 Publisher로 생각할 수 있다.
        log.info("pos2");
        return m;
    }

    public static String generateMessage() {
        log.info("generateMessage()");
        return "Hello Webflux";
    }
    public static void main(String[] args) {
        // https://stackoverflow.com/questions/46925508/default-number-of-threads-in-spring-boot-2-0-reactive-webflux-configuration
        System.setProperty("reactor.netty.ioWorkerCount", "1");
        System.setProperty("reactor.netty.pool.maxConnections", "2000");
        SpringApplication.run(MonoApplication.class, args);
    }
}
