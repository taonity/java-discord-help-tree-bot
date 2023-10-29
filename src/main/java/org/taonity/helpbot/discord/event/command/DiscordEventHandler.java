package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.taonity.helpbot.discord.MessageHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface DiscordEventHandler<E extends Event> {

    List<Predicate<E>> getFilterPredicates();
    default boolean filter(E event) {
        return getFilterPredicates().stream()
                .reduce(Predicate::and)
                .orElse(predicate -> true)
                .test(event);
    };

    default Mono<Void> reactiveHandle(E event) {
        handle(event);
        return Mono.empty();
    }

    void handle(E event);
}
