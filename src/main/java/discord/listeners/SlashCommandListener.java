package discord.listeners;

import discord.handler.command.AbstractSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements DiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<AbstractSlashCommand> commands;

    @Override
    public void handle(ChatInputInteractionEvent event) {
        log.info("Command {} [{}] received from user {} in guild {}",
                event.getCommandName(),
                event.getOptions().stream()
                        .map(ApplicationCommandInteractionOption::getValue)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(ApplicationCommandInteractionOptionValue::asString)
                        .collect(Collectors.joining(" ")),
                event.getInteraction().getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId()
                        .map(Snowflake::asString)
                        .orElse("NULL"));
        Flux.fromIterable(commands)
                .filter(commands -> commands.filter(event))
                .next()
                .flatMap(command -> command.reactiveHandle(event))
                .subscribe();
    }

}

