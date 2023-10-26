package org.taonity.helpbot.discord.event;

import discord4j.core.GatewayDiscordClient;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ListenerRunner implements CommandLineRunner {
    private final Collection<DiscordEventListener> eventListeners;
    private final GatewayDiscordClient client;

    @Override
    @SuppressWarnings("unchecked")
    public void run(String... args) throws Exception {
        eventListeners.forEach(listener ->
                client.on(listener.getGenericType(), listener::reactiveHandle).subscribe());
    }
}
