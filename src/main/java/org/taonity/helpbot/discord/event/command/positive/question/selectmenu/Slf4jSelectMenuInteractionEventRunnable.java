package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Member;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jSelectMenuInteractionEventRunnable extends Slf4jRunnable<SelectMenuInteractionEvent> {
    public Slf4jSelectMenuInteractionEventRunnable(SelectMenuInteractionEvent object) {
        super(object);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", getGuildId());
        MDC.put("userId", getMemberId());
    }

    private String getGuildId() {
        return object.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL");
    }

    private String getMemberId() {
        return object.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }
}
