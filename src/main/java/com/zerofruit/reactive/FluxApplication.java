package com.zerofruit.reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
@RestController
public class FluxApplication {
    @GetMapping("/event/{id}")
    public Mono<Event> event(@PathVariable long id) {
        return Mono.just(new Event(id, "event" + id));
    }

    @GetMapping("/events")
    public Flux<Event> events() {
        return Flux.fromStream(
                Stream.generate(() -> new Event(System.currentTimeMillis(), "event")))
                .take(10);
    }

    @GetMapping(value = "/events-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> eventsStream() {
        return Flux
                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "event")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @Data
    @AllArgsConstructor
    public static class Event {
        long id;
        String value;
    }

    public static void main(String[] args) {
        SpringApplication.run(FluxApplication.class, args);
    }
}
