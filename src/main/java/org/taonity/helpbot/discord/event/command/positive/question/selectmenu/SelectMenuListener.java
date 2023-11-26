package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;
import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.USER_ID_MDC_KEY;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Member;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.ExtendedDiscordEventListener;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements ExtendedDiscordEventListener<SelectMenuInteractionEvent> {

    private final Collection<AbstractSelectMenuHandler> handlers;

    @Override
    public ContextView getContextView(SelectMenuInteractionEvent event) {
        return Context.of(
                GUILD_ID_MDC_KEY, getGuildId(event),
                USER_ID_MDC_KEY, getMemberId(event));
    }

    private String getGuildId(SelectMenuInteractionEvent event) {
        return event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL");
    }

    private String getMemberId(SelectMenuInteractionEvent event) {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }
}
