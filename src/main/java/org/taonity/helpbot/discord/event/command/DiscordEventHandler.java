package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.Event;
import java.util.List;
import java.util.function.Function;
import reactor.core.publisher.Mono;

public interface DiscordEventHandler<E extends Event> {

    List<Function<E, Mono<Boolean>>> getFilterPredicates();

    default Mono<Boolean> filter(E event) {
        return Mono.defer(() -> {
            Mono<Boolean> result = Mono.just(true);

            for (Function<E, Mono<Boolean>> predicateFunction : getFilterPredicates()) {
                result = result.flatMap(isValid -> predicateFunction
                        .apply(event)
                        .map(isValid2 -> isValid && isValid2)
                        .filterWhen(Mono::just));
            }

            return result.switchIfEmpty(Mono.just(false));
        });
    }

    Mono<Void> handle(E event);
}
