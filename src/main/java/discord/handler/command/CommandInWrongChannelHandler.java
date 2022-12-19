package discord.handler.command;

import discord.exception.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.localisation.SimpleMessage;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CommandInWrongChannelHandler extends AbstractSlashCommand {

    @Getter
    private final CommandName command = CommandName.ANY;

    private final EventPredicates eventPredicates;
    private final MessageChannelService messageChannelService;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(this::filterByCommand)
                .filter(eventPredicates::filterBot)
                .filter(e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP))
                .filter(e -> !eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        final var messageString = event.getInteraction().getGuild().blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.HELP))
                .map(Channel::getMention)
                .map(mention -> String.format(SimpleMessage.COMMAND_IN_WRONG_CHANNEL_MESSAGE_FORMAT.getMessage(), mention))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20059));


        event.reply().withEmbeds(EmbedBuilder.buildSimpleMessage(
                messageString,
                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE
        )).subscribe();

    }
}
