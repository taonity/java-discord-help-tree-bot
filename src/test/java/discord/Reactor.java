package discord;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

class Reactor {

    public class SequenceGenerator {
        public Flux<Integer> generateFibonacciWithTuples() {
            return Flux.generate(
                    () -> Tuples.of(0, 1),
                    (state, sink) -> {
                        sink.next(state.getT1());
                        return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                    }
            );
        }
    }
    @Test
    void t1() {
        SequenceGenerator sequenceGenerator = new SequenceGenerator();
        Flux<Integer> fibonacciFlux = sequenceGenerator.generateFibonacciWithTuples().take(5);

        StepVerifier.create(fibonacciFlux)
                .expectNext(0, 1, 1, 2, 3)
                .expectComplete()
                .verify();
    }
}
