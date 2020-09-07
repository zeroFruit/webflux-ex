package com.zerofruit.reactive;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IteratorApplication {
    /*
    // 1. Iterable <---> Observable Duality
    public static void main(String[] args) {
        Iterable<Integer> iter = () -> new Iterator<Integer>() {
            private int start = 0;
            private final static int MAX = 10;
            @Override
            public boolean hasNext() {
                return start < MAX;
            }
            @Override
            public Integer next() {
                return ++start;
            }
        };
        for (Integer integer : iter) { // pulling data from Iterator
            System.out.println(integer);
        }
    }
    */

    /*
    // Observable -> Event/Data -> Observer
    // Run Observable from another thread
    static class IntegerObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i = 1; i <=10; i++) {
                setChanged();
                notifyObservers(i); // push data to observer
            }

        }
    }
    public static void main(String[] args) {
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " - " + arg);
            }
        };
        IntegerObservable io = new IntegerObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " - EXIT");
        es.shutdown();
    }

    */

    /*
    // Problem of Observable
    // 1. End of stream handling
    // 2. Error handling
    // ref: http://www.reactive-streams.org/
     */
    // Publisher <--> Observable
    // Subscriber <--> Observer

    /*
    public static void main(String[] args) {
        Publisher pub = new Publisher() {
            Iterable<Integer> it = Arrays.asList(1, 2, 3, 4, 5);

            // define who to publish
            @Override
            public void subscribe(Subscriber subscriber) {
                ExecutorService es = Executors.newSingleThreadScheduledExecutor();
                Iterator<Integer> iterator = it.iterator();

                subscriber.onSubscribe(new Subscription() {
                    Future<?> f;
                    @Override
                    public void request(long n) {
                        // task의 진행상황을 체크하고 싶을 땐 Future를 받아서 cancel하거나 할 수 있다.
                        //
                        // Future는 자바의 비동기 프로그래밍의 가장 기본적인 객체이다.
                        this.f = es.submit(() -> {
                            long left = n;
                            try {
                                while (left > 0) {
                                    if (!iterator.hasNext()) {
                                        subscriber.onComplete();
                                        es.shutdown();
                                        break;
                                    }
                                    // Publisher가 Subscriber.onNext()로 전달되는 값은 한 스레드에서만 값을 넘기는 것을 보장한다.
                                    subscriber.onNext(iterator.next());
                                    left -= 1;
                                }
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }
                    @Override
                    public void cancel() {
                        f.cancel(true);
                    }
                });
            }
        };

        Subscriber<Integer> sub = new Subscriber<Integer>() {
            final static int MAX_BUFFER_SIZE = 2;

            Subscription subscription;
            List<Integer> buf = new ArrayList<>();
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " - onSubscribe");
                this.subscription = subscription;
                this.subscription.request(MAX_BUFFER_SIZE);
            }
            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " - onNext - " + item);
                buf.add(item);
                if (buf.size() >= MAX_BUFFER_SIZE) {
                    System.out.println("bufferFlush");
                    System.out.println(buf);
                    buf = new ArrayList<>();
                    this.subscription.request(MAX_BUFFER_SIZE);
                }
            }
            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError - " + throwable);
            }
            @Override
            public void onComplete() {
                System.out.println("onComplete");
                System.out.println(buf);
                buf = new ArrayList<>();
            }
        };

        pub.subscribe(sub);
    }
    */

    /*
    public static void main(String[] args) {
//        Flux.create(e -> {
//            e.next(1);
//            e.next(2);
//            e.next(3);
//            e.complete();
//        }).subscribe(System.out::println);
    }

    @RestController
    public static class Controller {
        @RequestMapping("/hello")
        public Publisher<String> hello(String name) {
            return new Publisher<String>() {
                @Override
                public void subscribe(Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Subscription() {
                        @Override
                        public void request(long n) {
                            subscriber.onNext("Hello " + name);
                            subscriber.onComplete();
                        }
                        @Override
                        public void cancel() {
                        }
                    });
                }
            };
        }
    }

     */
}
