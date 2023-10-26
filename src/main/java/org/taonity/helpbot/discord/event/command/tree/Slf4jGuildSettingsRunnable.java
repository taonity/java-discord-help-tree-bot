package org.taonity.helpbot.discord.event.command.tree;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.GuildSettings;

@RequiredArgsConstructor
public class Slf4jGuildSettingsRunnable implements Runnable {
    private final GuildSettings guildSettings;
    private final Consumer<GuildSettings> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", guildSettings.getGuildId());
        eventConsumer.accept(guildSettings);
    }
}
