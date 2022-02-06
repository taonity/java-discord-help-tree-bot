package discord.listeners;

import discord.handler.message.MessageHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class MessageCreateListener implements DiscordEventListener<MessageCreateEvent> {

    private final Collection<MessageHandler> messageHandler;

    public void handle(MessageCreateEvent event) {
        Flux.fromIterable(messageHandler)
                .filter(handler -> handler.filter(event))
                .next()
                .flatMap(handler -> handler.reactiveHandle(event))
                .subscribe();
    }
}
