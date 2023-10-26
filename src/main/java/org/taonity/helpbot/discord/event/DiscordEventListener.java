package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.Event;
import org.springframework.core.GenericTypeResolver;
import reactor.core.publisher.Mono;

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
        getMdcAwareThreadPoolExecutor().submit(createSlf4jRunnable(event));
    }

    Runnable createSlf4jRunnable(E event);

    MdcAwareThreadPoolExecutor getMdcAwareThreadPoolExecutor();

    void handle(E event);
}
