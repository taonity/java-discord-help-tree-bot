package org.taonity.helpbot.discord.event.command.positive.config;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.GuildSettings;

@RequiredArgsConstructor
public class Slf4jWebhookEventRunnable implements Runnable {
    private final WebhookEvent guildCreateEvent;
    private final GuildSettings guildSettings;
    private final Consumer<WebhookEvent> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", guildSettings.getGuildId());
        eventConsumer.accept(guildCreateEvent);
    }
}
