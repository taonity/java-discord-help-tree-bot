package org.taonity.helpbot.discord.event.command;

import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.*;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.ExtendedDiscordEventListener;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements ExtendedDiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<SlashCommand> handlers;

    @Override
    public ContextView getContextView(ChatInputInteractionEvent event) {
        return Context.of(
                GUILD_ID_MDC_KEY, getGuildId(event),
                USER_ID_MDC_KEY, getMemberId(event),
                COMMAND_NAME_MDC_KEY, event.getCommandName());
    }

    @Override
    public Mono<Void> preHandle(ChatInputInteractionEvent event) {
        return Mono.<Void>empty()
                .tap(OnCompleteSignalListenerBuilder.of(
                        () -> log.info("Command received with parameters [{}]", getCommandParameters(event))));
    }

    private static String getCommandParameters(ChatInputInteractionEvent event) {
        return event.getOptions().stream()
                .map(ApplicationCommandInteractionOption::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .collect(Collectors.joining(" "));
    }

    private String getGuildId(ChatInputInteractionEvent event) {
        return event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL");
    }

    private String getMemberId(ChatInputInteractionEvent event) {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }
}
