package utils;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

public class MdcTest {
    @Test
    void testMDC() {
        Logger log = LoggerFactory.getLogger("reactor1");

        Hooks.enableAutomaticContextPropagation();
        ContextRegistry.getInstance().registerThreadLocalAccessor(new MdcAccessor());
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(
                        "guildId",
                        () -> MDC.get("guildId"),
                        value -> MDC.put("guildId", value),
                        () -> MDC.remove("guildId"));

        Mono.defer(() -> {
                    MDC.put("guildId", "Hello");
                    return Mono.just("Delayed");
                })
                .delayElement(Duration.ofMillis(10))
                .doOnSuccess(log::info)
                .block();
    }

    static class MdcAccessor implements ThreadLocalAccessor<Map<String, String>> {

        static final String KEY = "mdc";

        @Override
        public Object key() {
            return KEY;
        }

        @Override
        public Map<String, String> getValue() {
            return MDC.getCopyOfContextMap();
        }

        @Override
        public void setValue(Map<String, String> value) {
            MDC.setContextMap(value);
        }

        @Override
        public void reset() {
            MDC.clear();
        }
    }
}
