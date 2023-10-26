package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Member;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@RequiredArgsConstructor
public class Slf4jSelectMenuInteractionEventRunnable implements Runnable {
    private final SelectMenuInteractionEvent event;
    private final Consumer<SelectMenuInteractionEvent> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", getGuildId());
        MDC.put("userId", getMemberId());
        eventConsumer.accept(event);
    }

    private String getGuildId() {
        return event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL");
    }

    private String getMemberId() {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }
}
