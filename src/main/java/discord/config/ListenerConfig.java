package discord.config;

import discord.listeners.DiscordEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@RequiredArgsConstructor
public class ListenerConfig implements CommandLineRunner {
    private final Collection<DiscordEventListener> eventListeners;
    private final GatewayDiscordClient client;

    @Override
    @SuppressWarnings("unchecked")
    public void run(String... args) throws Exception {
        eventListeners.forEach(listener -> client.on(listener.getGenericType(), listener::reactiveHandle).subscribe());
    }
}
