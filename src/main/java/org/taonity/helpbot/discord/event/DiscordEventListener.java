package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.Event;
import org.springframework.core.GenericTypeResolver;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public interface DiscordEventListener<E extends Event> {
    @SuppressWarnings("unchecked")
    default Class<E> getGenericType() {
        return (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), DiscordEventListener.class);
    }

    default ContextView getContextView(E event) {
        return Context.empty();
    }
    ;

    Mono<Void> handle(E event);
}
