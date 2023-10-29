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
@Getter
public class MessageCreateListener implements ExtendedDiscordEventListener<MessageCreateEvent> {

    private final Collection<MessageHandler> handlers;

    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Slf4jRunnable<MessageCreateEvent> createSlf4jRunnable(MessageCreateEvent event) {
        return new Slf4jMessageCreateEventRunnable(event);
    }
}
