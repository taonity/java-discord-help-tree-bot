package org.taonity.helpbot.discord.event;

import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;
import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.USER_ID_MDC_KEY;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.MessageHandler;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Component
@RequiredArgsConstructor
@Getter
public class MessageCreateListener implements ExtendedDiscordEventListener<MessageCreateEvent> {

    private final Collection<MessageHandler> handlers;

    @Override
    public ContextView getContextView(MessageCreateEvent event) {
        return Context.of(
                GUILD_ID_MDC_KEY, getGuildId(event),
                USER_ID_MDC_KEY, getUserId(event));
    }

    private String getUserId(MessageCreateEvent event) {
        return event.getMember().map(User::getId).map(Snowflake::asString).orElse("NULL");
    }

    private String getGuildId(MessageCreateEvent event) {
        return event.getGuildId().map(Snowflake::asString).orElse("NULL");
    }
}
