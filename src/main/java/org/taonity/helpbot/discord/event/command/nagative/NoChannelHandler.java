package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractNegativeSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.SimpleMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoChannelHandler extends AbstractNegativeSlashCommand {

    @Getter
    private final List<CommandName> commands = List.of(CommandName.QUESTION, CommandName.CONFIG);

    private final EventPredicates eventPredicates;

    @Override
    public final List<Predicate<ChatInputInteractionEvent>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                this::filterByCommands,
                e -> !eventPredicates.filterIfChannelsExistInSettings(e)
        );
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(
                        SimpleMessage.NO_CHANNEL_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true)
                .withEphemeral(true)
                .subscribe();

        log.info("Command failed with non-configured channels");
    }
}
