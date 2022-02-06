package discord.handler;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface DiscordEventHandler<E extends Event> {
    boolean filter(E event);

    default Mono<Void> reactiveHandle(E event) {
        handle(event);
        return Mono.empty();
    }

    void handle(E event);
}
