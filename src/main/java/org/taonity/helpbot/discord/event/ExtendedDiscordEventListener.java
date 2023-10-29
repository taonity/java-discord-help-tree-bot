package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.Event;
import org.springframework.core.GenericTypeResolver;
import org.taonity.helpbot.discord.event.command.DiscordEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ExtendedDiscordEventListener<E extends Event> extends DiscordEventListener<E> {
    Collection<? extends DiscordEventHandler<E>> getHandlers();

    default void handleWithMdc(E event) {
        final var runnable = createSlf4jRunnable(event);
        runnable.setConsumer(this::filterAndHandle);
        getMdcAwareThreadPoolExecutor().submit(runnable);
    }

    default void filterAndHandle(E event) {
        handle(event);
        Flux.fromIterable(getHandlers())
                .filter(handler -> handler.filter(event))
                .next()
                .flatMap(handler -> handler.reactiveHandle(event))
                .subscribe();
    }
    default void handle(E event) {};
}
