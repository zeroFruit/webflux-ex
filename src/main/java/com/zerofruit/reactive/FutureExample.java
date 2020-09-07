//package com.zerofruit.reactive;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Objects;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;
//
//@Slf4j
//public class FutureExample {
//    interface SuccessCallback {
//        void onSuccess(String result);
//    }
//    interface ExceptionCallback {
//        void onError(Throwable t);
//    }
//    public static class CallbackFutureTask extends FutureTask<String> {
//        SuccessCallback sc;
//        ExceptionCallback ec;
//        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
//            super(callable);
//            this.sc = Objects.requireNonNull(sc);
//            this.ec = Objects.requireNonNull(ec);
//        }
//
//        @Override
//        protected void done() {
//            try {
//                sc.onSuccess(get());
//            } catch (InterruptedException e) {
//                // 다시 던져도 밖에서 처리할 방법이 없음.
//                // 인터럽트 예외는 현재 스레드가 인터럽트 되었다는 시그널을 주는 것만 해주면 된다.
//                Thread.currentThread().interrupt();
//            } catch (ExecutionException e) {
//                ec.onError(e.getCause());
//            }
//        }
//    }
//
//    /*
//    // Future, Callback - 비동기 작업의 결과를 가져오기위한 메커니즘
//
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ExecutorService es = Executors.newCachedThreadPool();
//
//        /*
//        Future<String> f = es.submit(() -> {
//            Thread.sleep(2000);
//            log.debug("hello");
//            return "result";
//        });
//        log.debug("{}", f.get()); // blocking
//        log.debug("exit");
//        es.shutdown();
//
//         */
//
//        /*
//        FutureTask<String> ft = new FutureTask<>(() -> {
//            Thread.sleep(2000);
//            log.debug("hello");
//            return "result";
//        }) {
//            @Override
//            protected void done() {
//                try {
//                    System.out.println(get());
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        es.submit(ft);
//        log.debug("{}", ft .get()); // blocking
//        log.debug("exit");
//        es.shutdown();
//
//         */
//        /*
//        CallbackFutureTask cft = new CallbackFutureTask(() -> {
//            Thread.sleep(2000);
//            log.debug("hello");
//            return "result";
//        }, r -> log.debug("onSuccess - {}", r), t -> log.error("onError", t));
//        es.submit(cft);
//        es.shutdown();
//    }
//    */
//}
