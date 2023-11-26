package utils;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ThreadTest {
    @Test
    void test() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                30,
                200,
                0,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                Thread::new,
                new ThreadPoolExecutor.AbortPolicy());

        Iterable<String> strings = List.of("a", "b", "c", "d");
        StreamSupport.stream(strings.spliterator(), false)
                .forEach(string -> threadPoolExecutor.submit(() -> logString(string)));
    }

    private static void logString(String string) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info(string);
    }
}
