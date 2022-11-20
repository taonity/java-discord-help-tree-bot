package discord.listeners;

import discord.messagehandlers.MessageHandler;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class MessageCreateListener implements DiscordEventListener<MessageCreateEvent> {

    private final Collection<MessageHandler> messageHandler;

    public Mono<Void> handle(MessageCreateEvent event) {
        return Flux.fromIterable(messageHandler)
                .filter(handler -> handler.condition(event))
                .next()
                .flatMap(handler -> {
                    handler.handle(event);
                    return Mono.empty();
                });
    }
}
