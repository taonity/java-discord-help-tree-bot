package org.taonity.helpbot.discord.mdc;

import java.util.function.Consumer;
import java.util.function.Supplier;
import reactor.core.observability.DefaultSignalListener;
import reactor.core.observability.SignalListener;

public class OnErrorSignalListenerBuilder {
    public static <T> Supplier<SignalListener<T>> of(Consumer<Throwable> consumer) {
        return () -> new DefaultSignalListener<>() {
            @Override
            public void doOnError(Throwable error) {
                consumer.accept(error);
            }
        };
    }
}
