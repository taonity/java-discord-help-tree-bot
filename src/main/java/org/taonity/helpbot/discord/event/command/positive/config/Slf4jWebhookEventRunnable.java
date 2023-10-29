package org.taonity.helpbot.discord.event.command.positive.config;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jWebhookEventRunnable extends Slf4jRunnable<WebhookEvent> {
    private final GuildSettings guildSettings;

    public Slf4jWebhookEventRunnable(WebhookEvent object, GuildSettings guildSettings, Consumer<WebhookEvent> consumer) {
        super(object);
        this.guildSettings = guildSettings;
        setConsumer(consumer);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", guildSettings.getGuildId());
    }
}
