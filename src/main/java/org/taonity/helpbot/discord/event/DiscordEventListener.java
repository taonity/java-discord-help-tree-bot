package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.Event;
import org.springframework.core.GenericTypeResolver;
import org.taonity.helpbot.discord.event.command.DiscordEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface DiscordEventListener<E extends Event> {
    @SuppressWarnings("unchecked")
    default Class<E> getGenericType() {
        return (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), DiscordEventListener.class);
    }

    default Mono<Void> reactiveHandle(E event) {
        handleWithMdc(event);
        return Mono.empty();
    }

    default void handleWithMdc(E event) {
        final var runnable = createSlf4jRunnable(event);
        runnable.setConsumer(this::handle);
        getMdcAwareThreadPoolExecutor().submit(runnable);
    }

    Slf4jRunnable<E> createSlf4jRunnable(E event);

    MdcAwareThreadPoolExecutor getMdcAwareThreadPoolExecutor();

    void handle(E event);
}
