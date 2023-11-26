package org.taonity.helpbot.discord.mdc;

import java.util.function.Supplier;
import reactor.core.observability.DefaultSignalListener;
import reactor.core.observability.SignalListener;

public class OnCompleteSignalListenerBuilder {
    public static <T> Supplier<SignalListener<T>> of(Runnable runnable) {
        return () -> new DefaultSignalListener<>() {
            @Override
            public void doOnComplete() {
                runnable.run();
            }
        };
    }
}
