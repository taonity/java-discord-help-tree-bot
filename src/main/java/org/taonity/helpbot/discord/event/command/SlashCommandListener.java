package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.ExtendedDiscordEventListener;
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import org.taonity.helpbot.discord.event.Slf4jRunnable;
import reactor.core.publisher.Flux;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements ExtendedDiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<SlashCommand> handlers;

    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Slf4jRunnable<ChatInputInteractionEvent> createSlf4jRunnable(ChatInputInteractionEvent event) {
        return new Slf4jChatInputInteractionEventRunnable(event);
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        log.info("Command received with parameters [{}]", getCommandParameters(event));
    }

    private static String getCommandParameters(ChatInputInteractionEvent event) {
        return event.getOptions().stream()
                .map(ApplicationCommandInteractionOption::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .collect(Collectors.joining(" "));
    }
}