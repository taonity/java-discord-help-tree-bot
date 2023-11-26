package utils;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

class ReactorTest {

    public static class SequenceGenerator {
        public Flux<Integer> generateFibonacciWithTuples() {
            return Flux.generate(() -> Tuples.of(0, 1), (state, sink) -> {
                sink.next(state.getT1());
                return Tuples.of(state.getT2(), state.getT1() + state.getT2());
            });
        }
    }

    @Test
    @Disabled
    void t1() {
        SequenceGenerator sequenceGenerator = new SequenceGenerator();
        Flux<Integer> fibonacciFlux =
                sequenceGenerator.generateFibonacciWithTuples().take(5);

        StepVerifier.create(fibonacciFlux)
                .expectNext(0, 1, 1, 2, 3)
                .expectComplete()
                .verify();
    }

    @Test
    @Disabled
    void t2() {
        Flux.fromIterable(List.of(1, 2, 3, 4, 5)).flatMap(ReactorTest::processInteger);
    }

    @Test
    @Disabled
    void t3() {
        Mono.empty()
                .then(Mono.just(Collections.<String>emptyList())
                        .flatMapMany(Flux::fromIterable)
                        .concatMap(s -> Mono.just(s.toLowerCase()))
                        .then())
                .then(Mono.defer(() -> {
                    System.out.println("0");
                    return Mono.empty();
                }))
                .doOnSuccess(s -> System.out.println("1"))
                .doOnCancel(() -> System.out.println("2"))
                .block();
    }

    @Test
    @Disabled
    void t4() {
        Flux.zip(Mono.just(List.of(1, 2, 3)), Mono.just(List.of(4, 5, 6)))
                .next()
                .flatMap(t -> {
                    System.out.println(t.getT1());
                    System.out.println(t.getT2());
                    return Mono.empty();
                })
                .then()
                .block();
    }

    @NotNull
    private static Mono<Integer> processInteger(Integer integer) {
        if (integer == 3) {
            return Mono.error(new RuntimeException("Error!"));
        } else {
            integer *= 2;
            System.out.println(integer);
            return Mono.just(integer);
        }
    }
}
