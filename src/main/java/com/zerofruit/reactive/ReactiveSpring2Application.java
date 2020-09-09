package com.zerofruit.reactive;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.LoopResources;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
@EnableAsync
public class ReactiveSpring2Application {

    @Bean
    public NioEventLoopGroup nioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    @Qualifier("test")
    public ReactorResourceFactory reactorResourceFactory(NioEventLoopGroup lg) {
        ReactorResourceFactory f = new ReactorResourceFactory();
        f.setLoopResources(useNative -> lg);
        f.setUseGlobalResources(false);
        return f;
    }

    @Bean
    public WebClient webClient(@Qualifier("test") ReactorResourceFactory f) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(f, m -> m))
                .build();
    }

    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/asyncwork");
        }
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(1);
        te.initialize();
        return te;
    }

    @RestController
    public static class MyController {

        @Autowired
        WebClient wc;

        @Autowired
        MyService myService;

        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(
                new NioEventLoopGroup(1)));

        static final String URL1 = "http://localhost:8081/service1?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            Completion
                    .from(rt.getForEntity(URL1, String.class, "h" + idx))
                    .andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
                    .andApply(s -> myService.work(s.getBody()))
                    .andError(e -> dr.setErrorResult(e.toString()))
                    .andAccept(s -> dr.setResult(s));
            return dr;
        }
    }

    public static class AcceptCompletion<T> extends Completion<T, Void> {
        private Consumer<T> con;

        public AcceptCompletion(Consumer<T> con) {
            this.con = con;
        }

        @Override
        public void run(T value) {
            con.accept(value);
        }
    }

    public static class ApplyCompletion<S, T> extends Completion<S, T> {
        private Function<S, ListenableFuture<T>> fn;

        public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
            this.fn = fn;
        }

        @Override
        public void run(S value) {
            ListenableFuture<T> lf = fn.apply(value);
            lf.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onFailure(Throwable ex) {
                    error(ex);
                }

                @Override
                public void onSuccess(T result) {
                    complete(result);
                }
            });
        }
    }

    public static class ErrorCompletion<T> extends Completion<T, T> {
        private Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        public void run(T value) {
            if (next != null) {
                next.run(value);
            }
        }

        @Override
        void error(Throwable ex) {
            econ.accept(ex);
        }
    }

    public static class Completion<S, T> {
        protected Completion next;

        public Completion() {}


        public void andAccept(Consumer<T> con) {
            AcceptCompletion<T> c = new AcceptCompletion(con);
            this.next = c;
        }

        public Completion<T, T> andError(Consumer<Throwable> econ) {
            ErrorCompletion c = new ErrorCompletion(econ);
            this.next = c;
            return c;
        }

        public <U> Completion<T, U> andApply(Function<T, ListenableFuture<U>> fn) {
            ApplyCompletion<T, U> c = new ApplyCompletion(fn);
            this.next = c;
            return c;
        }

        static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
            Completion c = new Completion<>();
            lf.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onFailure(Throwable ex) {
                    c.error(ex);
                }

                @Override
                public void onSuccess(T result) {
                    c.complete(result);
                }
            });
            return c;
        }

        void complete(T s) {
            if (next != null) {
                next.run(s);
            }
        }

        void run(S value) { }

        void error(Throwable ex) {
            if (next != null) {
                next.error(ex);
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSpring2Application.class, args);
    }

    // 메모리 사용량과의 관계는 얼마나 객체를 만드느냐와 관련이있고, 스레드를 많이 만든다면
    // 메모리 사용량이 증가할 것이다. 하지만 ListenableFuture를 사용한 비동기 처리방식은
    // worker thread를 많이 만들지 않기 때문에 thread과 관련된 메모리 부은 적다.
}
