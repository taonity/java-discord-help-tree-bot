package discord.listeners;

import discord.commands.SlashCommand;
import discord.messagehandlers.MessageHandler;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class MessageCreateListener {

    private final Collection<MessageHandler> messageHandler;

    public MessageCreateListener(GatewayDiscordClient client, Collection<MessageHandler> messageHandler) {
        this.messageHandler = messageHandler;

        client.on(MessageCreateEvent.class, this::handle).subscribe();
    }

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
