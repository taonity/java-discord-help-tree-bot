package org.taonity.helpbot.discord.event.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jChatInputInteractionEventRunnable extends Slf4jRunnable<ChatInputInteractionEvent> {
    public Slf4jChatInputInteractionEventRunnable(ChatInputInteractionEvent object) {
        super(object);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", getGuildId());
        MDC.put("userId", getMemberId());
        MDC.put("commandName", object.getCommandName());
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
