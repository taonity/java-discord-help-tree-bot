package org.taonity.helpbot.discord.config;

import discord4j.core.GatewayDiscordClient;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.taonity.helpbot.discord.event.DiscordEventListener;

@Configuration
@RequiredArgsConstructor
public class ListenerConfig implements CommandLineRunner {
    private final Collection<DiscordEventListener> eventListeners;
    private final GatewayDiscordClient client;

    @Override
    @SuppressWarnings("unchecked")
    public void run(String... args) throws Exception {
        eventListeners.forEach(listener ->
                client.on(listener.getGenericType(), listener::reactiveHandle).subscribe());
    }
}
