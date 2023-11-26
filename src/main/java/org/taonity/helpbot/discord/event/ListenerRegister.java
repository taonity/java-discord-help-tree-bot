package org.taonity.helpbot.discord.event;

import discord4j.core.GatewayDiscordClient;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListenerRegister {

    private final Collection<DiscordEventListener> eventListeners;

    private final GatewayDiscordClient client;

    @SuppressWarnings("unchecked")
    public void init() {
        eventListeners.forEach(listener -> client.on(listener.getGenericType(), event -> listener.handle(event)
                        .contextWrite(listener.getContextView(event)))
                .subscribe());
    }
}
