package org.taonity.helpbot.discord.event.command.tree;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jGuildSettingsRunnable extends Slf4jRunnable<GuildSettings> {
    public Slf4jGuildSettingsRunnable(GuildSettings object, Consumer<GuildSettings> consumer) {
        super(object);
        setConsumer(consumer);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", object.getGuildId());
    }
}
