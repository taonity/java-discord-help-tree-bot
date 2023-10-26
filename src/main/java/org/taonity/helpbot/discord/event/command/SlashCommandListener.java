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
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements DiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<SlashCommand> commands;

    @Getter
    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Runnable createSlf4jRunnable(ChatInputInteractionEvent event) {
        return new Slf4jChatImputInteractionEventRunnable(event, this::handle);
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        log.info("Command received with parameters [{}]", getCommandParameters(event));

        Flux.fromIterable(commands)
                .filter(commands -> commands.filter(event))
                .next()
                .flatMap(command -> command.reactiveHandle(event))
                .subscribe();
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
