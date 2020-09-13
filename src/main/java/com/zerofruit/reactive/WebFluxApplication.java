package com.zerofruit.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
@EnableAsync
public class WebFluxApplication {

    // run on Netty
    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        return new NettyReactiveWebServerFactory();
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(WebFluxApplication.class, args);
    }

    @RestController
    public static class MyController {

        WebClient wc = WebClient.create();

        @Autowired
        private MyService2 svc;

        static final String URL1 = "http://localhost:8081/service1?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        @GetMapping("/rest2")
        public Mono<String> rest(int idx) {
            // Mono는 Publisher의 구현체이다. 그래서 단순히 Mono 객체를 만드는 것만으로는 내부의 코드가 동작하지 않는다.
            // 아래에서 WebClient를 이용한 HTTP 호출은 되지 않는다.
            // Mono<ClientResponse> res = wc.get().uri(URL1, idx).exchange();

            // 우리가 직접 subscribe할 필요가 없고 Webflux 프레임워크가 리턴타입을 보고 Mono라면
            // 프레잌워크가 subscribe 해준다. 그 때 해당 Mono가 동작한다.

            // Mono라는 컨테이너에 계속 데이터가 담겨서 이동/변환된다.
            return wc.get().uri(URL1, idx).exchange()
                    // ClientResponse -> Mono<ClientResponse>
                    // .map(clientResponse -> clientResponse.bodyToMono(String.class));
                    .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                    .flatMap(res -> wc.get().uri(URL2, res).exchange())
                    .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                    .doOnNext(r -> log.info(r)) // reactor netty worker thread 위에서 동작
                    .flatMap(s -> Mono.fromCompletionStage(svc.work(s)))
                    .doOnNext(r -> log.info(r)); // spring async worker thread 위에서 동작

            // 어떤 스레드(스케쥴)에서 동작시킬 것인를 잘 생각해야함.
        }
    }

    @Service
    public static class MyService2 {
        @Async
        public CompletableFuture<String> work(String req) {
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }
}
