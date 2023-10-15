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
        handle(event);
        return Mono.empty();
    }

    void handle(E event);
}
