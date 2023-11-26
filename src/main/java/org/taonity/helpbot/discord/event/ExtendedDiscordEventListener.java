package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.Event;
import java.util.Collection;
import org.taonity.helpbot.discord.event.command.DiscordEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExtendedDiscordEventListener<E extends Event> extends DiscordEventListener<E> {
    Collection<? extends DiscordEventHandler<E>> getHandlers();

    default Mono<Void> handle(E event) {
        return preHandle(event)
                .then(Flux.fromIterable(getHandlers())
                        .filterWhen(handler -> handler.filter(event))
                        .next()
                        .flatMap(handler -> handler.handle(event)));
    }

    default Mono<Void> preHandle(E event) {
        return Mono.empty();
    }
    ;
}
