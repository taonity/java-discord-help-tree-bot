package org.taonity.helpbot.discord.event;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.MessageHandler;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class MessageCreateListener implements DiscordEventListener<MessageCreateEvent> {

    private final Collection<MessageHandler> messageHandler;

    @Getter
    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Runnable createSlf4jRunnable(MessageCreateEvent event) {
        return new Slf4jMessageCreateEventRunnable(event, this::handle);
    }

    public void handle(MessageCreateEvent event) {
        Flux.fromIterable(messageHandler)
                .filter(handler -> handler.filter(event))
                .next()
                .flatMap(handler -> handler.reactiveHandle(event))
                .subscribe();
    }
}
